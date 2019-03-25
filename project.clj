(defproject adserver "0.1.0-SNAPSHOT"
  :description     "Base ad-matching machinery"
  :url             "https://kapware.com/adserver"
  :license         {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
                    :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies    [[org.clojure/clojure       "1.10.0"]
                    [metosin/compojure-api     "2.0.0-alpha29"]
                    [org.clojure/tools.logging "0.4.1"]
                    [mount                     "0.1.16"]
                    [http-kit                  "2.3.0"]]
  :main ^:skip-aot adserver.core
  :target-path     "target/%s"
  :profiles        {:uberjar {:aot :all}
                    :dev     {:dependencies [[ring/ring-mock             "0.3.2"]
                                             [org.clojure/test.check     "0.9.0"]
                                             [com.gfredericks/test.chuck "0.2.9"]]}})
