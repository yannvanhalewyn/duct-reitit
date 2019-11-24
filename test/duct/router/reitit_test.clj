(ns duct.router.reitit-test
  (:require [duct.router.reitit]
            [clojure.test :refer [deftest testing is]]
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
   :duct.router/reitit {:routes ["/"
                                 ["hello/:name" {:handler (ig/ref ::handler.hello)}]
                                 ["math" {:parameters {:query {:x int? :y int?}}
                                          :handler (ig/ref ::handler.math)}]
                                 ["admin" {:handler (ig/ref ::handler.hello)
                                           :middleware [::admin]}]]
                        :middleware {::admin wrap-admin}}})

(deftest router-test
  (let [handler (:duct.router/reitit (ig/init config))]
    (testing "It hooks up the routes and handlers correctly"
      (is (= {:status 200 :headers {} :body "Hello world"}
             (handler {:uri "/hello/world"
                       :request-method :get}))))

    (testing "It can coerce the input params"
      (is (= {:status 200 :headers {} :body "12"}
             (handler {:uri "/math"
                       :request-method :get
                       :query-params {:x "3" :y "4"}}))))

    (testing "It uses the middleware registry"
      (let [req #(handler {:uri "/admin"
                           :request-method :get
                           :query-params {:role %}})]
        (is (= 200 (:status (req "admin"))))
        (is (= 403 (:status (req "user"))))))))
