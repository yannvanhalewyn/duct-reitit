# Duct module.reitit

A [Duct][] module that sets [Reitit][] as the router for your
application.

[duct]: https://github.com/duct-framework/duct
[reitit]: https://github.com/metosin/reitit

## Installation

To install, add the following to your project `:dependencies`:

    [duct/module.reitit "0.0.1"]

## Usage

### Basic

To add this module to your configuration, add the
`:duct.module/reitit` key. For example:

```edn
{:duct.core/project-ns my-app
 :duct.module/reitit {"/" :index}
 :my-app.handler/index {}}
```

The `:duct.module/reitit` key should contain a map of Reitit
routes. See the [route syntax][] section of Reitit's documentation for
more information on the format it expects.

The module uses the `:duct.core/project-ns` key and the result key to
find an appropriate Integrant key at:

    <project-ns>.handler[.<result key namespace>]/<result key name>

So in the above example, the project namespace is `my-app` and the only
result key is `:index`, so the module looks for a `:my-app.handler/index`
Integrant key.

If the result key was `:example/index` instead, then the Integrant key
would be `:my-app.handler.example/index`.

Similarly, the module looks for middleware at:

    <project-ns>.middleware[.<metadata key namespace>]/<metadata key name>

For example:

```edn
{:duct.core/project-ns   my-app
 :duct.module/reitit     {"/" {:name :index
                               :middleware [:example]}
 :my-app.handler/index      {}
 :my-app.middleware/example {}}
```

[route syntax]: https://metosin.github.io/reitit/basics/route_syntax.html

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
