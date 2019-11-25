(ns duct.router.reitit-test
  (:require [duct.router.reitit :as duct.reitit]
            [clojure.test :refer [deftest testing is]]
            [reitit.ring :as ring]
            [integrant.core :as ig]))

(defmethod ig/init-key ::handler.hello [_ _]
  (fn [{:keys [path-params]}]
    {:status 200
     :headers {}
     :body (str "Hello " (:name path-params))}))

(defmethod ig/init-key ::handler.math [_ _]
  (fn [{:keys [parameters]}]
    (let [{:keys [x y]} (:query parameters)]
      {:status 200
       :headers {}
       :body (str (* x y))})))

(defmethod ig/init-key ::handler.not-found [_ _]
  (fn [_] {:status 404 :headers {} :body "Not found"}))

(defmethod ig/init-key ::handler.bad-request [_ _]
  (fn [_] {:status 400 :headers {} :body "Bad request"}))

(defn- wrap-admin [handler]
  (fn [{:keys [query-params] :as req}]
    (if (= "admin" (:role query-params))
      (handler req)
      {:status 403
       :headers {}
       :body "Not admin"})))

(def config
  {::handler.hello {}
   ::handler.math {}
   ::handler.bad-request {}
   ::handler.not-found {}
   :duct.router/reitit {:routes ["/"
                                 ["hello/:name" {:handler (ig/ref ::handler.hello)}]
                                 ["math" {:get {:parameters {:query {:x 'int? :y 'int?}}
                                                :handler (ig/ref ::handler.math)}}]
                                 ["admin" {:handler (ig/ref ::handler.hello)
                                           :middleware [::admin]}]]
                        ::ring/opts {:reitit.middleware/registry {::admin wrap-admin}}
                        ::ring/default-handlers
                        {:not-acceptable     (ig/ref ::handler.bad-request)
                         :not-found          (ig/ref ::handler.not-found)
                         :method-not-allowed (ig/ref ::handler.not-found)}}})

(deftest prep-test
  (let [prep #(:duct.router/reitit (ig/prep {:duct.router/reitit %}))]

    (testing "It uses default coercers, handlers and extra opts"
      (is (= {:routes ["/"]
              ::ring/opts {:data duct.reitit/default-route-opts
                           :my-key "my-val"}
              ::ring/default-handlers duct.reitit/default-default-handlers}
             (prep {:routes ["/"] ::ring/opts {:my-key "my-val"}}))))

    (testing "It can overwrite default coercers and handlers"
      (is (= {:routes ["/"]
              ::ring/opts {:data {}}
              ::ring/default-handlers
              {:not-found :some-handler
               :not-acceptable :other-handler
               :method-not-allowed (ig/ref :duct.handler.static/method-not-allowed)}}
             (prep {:routes ["/"]
                    ::ring/opts {:data {}}
                    ::ring/default-handlers {:not-found :some-handler
                                             :not-acceptable :other-handler}}))))

    (testing "It resolves any symbols to clojure core"
      (is (= ["/" {:parameters {:query {:int? int?
                                        :string? string?
                                        :blank? clojure.string/blank?
                                        :other 'other}}}]
             (:routes
              (prep {:routes ["/" {:parameters {:query {:int? 'int?
                                                        :string? 'string?
                                                        :blank? 'clojure.string/blank?
                                                        :other 'other}}}]})))))))

(deftest router-test
  (let [handler (:duct.router/reitit (ig/init (ig/prep config)))
        req (fn [method uri & [params]]
              (handler {:uri uri
                        :request-method method
                        :query-params params}))]

    (testing "It hooks up the routes and handlers correctly"
      (is (= {:status 200 :headers {} :body "Hello world"}
             (req :get "/hello/world"))))

    (testing "It can coerce the input params"
      (is (= {:status 200 :headers {} :body "12"}
             (req :get "/math" {:x "3" :y "4"}))))

    (testing "It uses the middleware registry"
      (is (= 200 (:status (req :get "/admin" {:role "admin"}))))
      (is (= 403 (:status (req :get "/admin" {:role "user"})))))

    (testing "It uses default handlers"
      (is (= "Not found" (:body (req :get "/not-found"))))
      (is (= "Not found" (:body (req :post "/math")))))))
