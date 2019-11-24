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

(def duct-default-handlers
  {:not-found          (ig/ref :duct.handler.static/not-found)
   :not-acceptable     (ig/ref :duct.handler.static/bad-request)
   :method-not-allowed (ig/ref :duct.handler.static/method-not-allowed)})

(defmethod ig/prep-key :duct.router/reitit [_ {:keys [routes]
                                               ::ring/keys [opts default-handlers]}]
  {:routes routes
   ::ring/opts (merge {:data default-route-opts} opts)
   ::ring/default-handlers (merge duct-default-handlers default-handlers)})

(defmethod ig/init-key :duct.router/reitit [_ {:keys [routes]
                                               ::ring/keys [opts default-handlers]}]
  (ring/ring-handler
   (ring/router routes opts)
   (ring/create-default-handler default-handlers)))
