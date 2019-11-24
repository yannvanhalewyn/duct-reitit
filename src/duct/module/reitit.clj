(ns duct.module.reitit
  (:require [clojure.string :as str]
            [duct.core :as duct]
            [integrant.core :as ig]
            [reitit.core :as reitit]
            [reitit.ring]))

(defn prefix-keyword
  "Prepends the namespace of keyword with all prefixes joined by a .

   ``` clojure
    (prefix-keyword :users/index 'my-ns \"handler\")
     ;; => :my-ns.handler.users/index
    ```"
  [kw & prefixes]
  (let [ns* (str (str/join "." prefixes)
                 (when-let [n (namespace kw)]
                   (str "." n)))]
    (keyword ns* (name kw))))

(defn- infer-handler [project-ns route]
  (let [kw (prefix-keyword (:name route) project-ns "handler")]
    (assoc route :handler (ig/ref kw))))

(defn- infer-middleware [project-ns route]
  (if (contains? route :middleware)
    (let [->ref #(ig/ref (prefix-keyword % project-ns "middleware"))]
      (update route :middleware #(map ->ref %)))
    route))

(defn infer-expander [project-ns]
  (fn [this opts]
    (->> (reitit/expand this opts)
         (infer-handler project-ns)
         (infer-middleware project-ns))))

(defmethod ig/init-key :duct.module/reitit [_ routes]
  (fn [config]
    (let [routes     (dissoc routes ::duct/requires)
          expander   (infer-expander (:duct.core/project-ns config))
          demote     #(with-meta % {:demote true})]
      (duct/merge-configs
       config
       {:duct.handler/root {:router (ig/ref :duct.router/reitit)}
        :duct.router/reitit {:routes (demote routes)
                             :reitit.ring/opts {:expand (demote expander)}}}))))
