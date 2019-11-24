(ns duct.router.reitit-test
  (:require [duct.router.reitit]
            [clojure.test :refer [deftest testing is]]
            [integrant.core :as ig]))

(defmethod ig/init-key ::handler.hello [_ _]
  (fn [{:keys [path-params]}]
    {:status 200
     :headers {}
     :body (str "Hello " (:name path-params))}))

(deftest router-test
  (let [config  {::handler.hello {}
                 :duct.router/reitit
                 {:routes   ["/hello/:name" {:get {:name string?}
                                             :responses {200 {:body string?}}
                                             :handler (ig/ref ::handler.hello)}]}}
        handler (:duct.router/reitit (ig/init config))]
    (is (= {:status 200 :headers {} :body "Hello world"}
           (handler {:uri "/hello/world"
                     :request-method :get})))))

