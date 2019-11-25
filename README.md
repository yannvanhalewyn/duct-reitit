# Duct Reitit

A [Duct][] component that uses [Reitit][] as the router for your
application.

[duct]: https://github.com/duct-framework/duct
[reitit]: https://github.com/metosin/reitit

## Installation

To install, add the following to your project `:dependencies`:

    [duct/reitit "0.0.1"]

## Usage

### Basic

To use this router, add the `:duct.router/reitit` key and reference it
from `:duct.core/handler` to use it.

```edn
{:duct.profile/base
 {:duct.core/project-ns my-app
  :duct.handler/root
  {:router #ig/ref :duct.router/reitit}
  :duct.router/reitit
  {:routes ["/" {:handler #ig/ref :my-app.handler/index
                 :middleware [#ig/ref :my-app.middleware/example]}]
   :reitit.ring/opts { ;; Add extra opts to be passed in to reitit.ring/router
                      }}
  :my-app.handler/index {}
  :my-app.middleware/example {}}}
```

See the [route syntax][] section of Reitit's documentation for
more information on the format it expects.

Reitit will assoc the router in the request by default. This is useful
for if you need to generate the path for a given route name.

```clojure
(require '[reitit.core :as reitit])

(defn my-handler [{::reitit/keys [router]}]
  (:path (reitit/match-by-name router :user/show {:id 1}))
  ;; => "/users/1"
  )
```

[route syntax]: https://metosin.github.io/reitit/basics/route_syntax.html
