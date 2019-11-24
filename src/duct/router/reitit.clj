(ns duct.router.reitit
  (:require [reitit.core :as r]
            [reitit.ring :as ring]
            [integrant.core :as ig]))

(defmethod ig/init-key :duct.router/reitit [_ {:keys [routes]}]
  (ring/ring-handler (ring/router routes)))
