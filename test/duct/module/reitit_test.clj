(ns duct.module.reitit-test
  (:require [clojure.test :refer [deftest is testing]]
            [duct.core :as duct]
            [duct.module.reitit]
            [integrant.core :as ig]))

(duct/load-hierarchy)

(defn user-handler [{:keys [path-params]}]
  {:status 200, :headers {}, :body (str "user " (:id path-params))})

(defn wrap-header [handler]
  #(assoc-in (handler %) [:headers "X-App"] "X"))

(def config
  {:duct.profile/base
   {:duct.core/project-ns 'my-app
    :my-app.handler.user/show user-handler
    :my-app.middlware.user/wrap-header wrap-header}
   :duct.module/reitit
   {"/" ["users/:id" {:name :user/show
                      :middleware [:user/wrap-header]}]}})

(deftest module-test
  (let [config (duct/build-config config)]

    (testing "It install the reitit router"
      (is (= {:router (ig/ref :duct.router/reitit)}
             (:duct.handler/root config)))
      (is (contains? config :duct.router/reitit)))

    ;; Might need a component test to verify all is working together
    (testing "It adds the expander to infer routes and middleware"
      (let [expander (get-in config [:duct.router/reitit :reitit.ring/opts :expand])]

        (testing "It infers handlers correctly"
          (is (= {:name :foo :handler (ig/ref :my-app.handler/foo)}
                 (expander :foo {})))
          (is (= {:name :user/show :handler (ig/ref :my-app.handler.user/show)}
                 (expander {:name :user/show} {}))))

        (testing "It infers middleware correctly"
          (is (= {:name :user/show
                  :handler (ig/ref :my-app.handler.user/show)
                  :middleware [(ig/ref :my-app.middleware.auth/admin)
                               (ig/ref :my-app.middleware/wrap)]}
                 (expander {:name :user/show
                            :middleware [:auth/admin :wrap]}
                           {}))))))))
