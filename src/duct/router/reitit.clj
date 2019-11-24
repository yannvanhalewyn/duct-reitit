(ns duct.router.reitit
  (:require [reitit.core :as r]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as ring.coercion]
            [reitit.coercion.spec :as coercion.spec]
            [duct.core.resource]
            [integrant.core :as ig]))

(def ^:private default-middleware
  [ring.coercion/coerce-exceptions-middleware
   ring.coercion/coerce-request-middleware
   ring.coercion/coerce-response-middleware])

(defmethod ig/init-key :duct.router/reitit [_ {:keys [routes]}]
  (ring/ring-handler
   (ring/router
    routes
    {:data {:coercion coercion.spec/coercion
            :middleware default-middleware}})))
