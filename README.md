# Duct module.reitit

A [Duct][] module that sets [Reitit][] as the router for your
application.

[duct]: https://github.com/duct-framework/duct
[ataraxy]: https://github.com/metosin/reitit

## Installation

To install, add the following to your project `:dependencies`:

    [duct/module.reitit "0.0.1"]

## Usage

### Advanced

If you want more control, you can use the router directly, without the
use of the module. To do this, reference the `:duct.router/reitit`
key from `:duct.core/handler`:

```edn
{:duct.core/handler
 {:router #ig/ref :duct.router/reitit}
 :duct.router/reitit
 {:routes {"/" {:handler #ig/ref :foo.handler/index
                :middleware [#ig/ref :foo.middleware/example]}}
  :reitit.ring/opts { ;; Add extra opts to be passed in to reitit.ring/router
                     }}
 :foo.handler/index {}
 :foo.middleware/example {}}
```
