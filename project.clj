(defproject photolog "0.1.0-SNAPSHOT"
  :description "A simple infinite scroll photolog application"
  :url ""
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [lib-noir "0.9.4"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 ;[markdown-clj "0.9.58"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [ring-server "0.3.1"]]
  :plugins [[lein-ring "0.8.12"]]
  :ring {:handler photolog.handler/app
         :init photolog.handler/init
         :destroy photolog.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]}})
