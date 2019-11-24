(ns duct.router.reitit
  (:require [reitit.ring :as ring]
            [reitit.ring.coercion :as ring.coercion]
            [reitit.coercion.spec :as coercion.spec]
            [duct.core.resource]
            [integrant.core :as ig]))

(def default-route-opts
  {:coercion coercion.spec/coercion
   :middleware [ring.coercion/coerce-exceptions-middleware
                ring.coercion/coerce-request-middleware
                ring.coercion/coerce-response-middleware]})

(defmethod ig/prep-key :duct.router/reitit [_ {:keys [routes] ::ring/keys [opts]}]
  {:routes routes
   ::ring/opts (merge {:data default-route-opts} opts)})

(defmethod ig/init-key :duct.router/reitit [_ {:keys [routes] ::ring/keys [opts]}]
  (ring/ring-handler (ring/router routes opts)))
