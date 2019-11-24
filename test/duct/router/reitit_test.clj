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

(def config
  {::handler.hello {}
   ::handler.math {}
   :duct.router/reitit {:routes ["/"
                                 ["hello/:name" {:handler (ig/ref ::handler.hello)}]
                                 ["math" {:get {:parameters {:query {:x int? :y int?}}
                                                :handler (ig/ref ::handler.math)}}]]}})

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
                       :query-params {:x "3" :y "4"}}))))))

