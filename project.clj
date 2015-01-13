(defproject photolog "0.1.0-SNAPSHOT"
  :description "A simple infinite scroll photolog application"
  :url ""
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.1.6"]
                 [hiccup "1.0.5"]
                 [lib-noir "0.9.4"]
                 [environ "0.4.0"]
                 [postgresql/postgresql "9.1-901.jdbc4"]
                 ;[markdown-clj "0.9.58"]
                 [ring.middleware.logger "0.5.0"]
                 [com.taoensso/timbre "3.3.1-1cd4b70"]
                 [com.postspectacular/rotor "0.1.0"]
                 [peridot "0.3.1"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [org.clojure/java.jdbc "0.3.6"]
                 [ring-server "0.3.1"]]
  :plugins [[lein-ring "0.8.12"] [lein-environ "0.4.0"]]
  :ring {:handler photolog.handler/app
         :init photolog.handler/init
         :destroy photolog.handler/destroy}
  :profiles
  {:uberjar {:aot :all}
   :production
   {:ring
    {:open-browser? false, :stacktraces? false, :auto-reload? false}}
   :dev
   {:dependencies [[ring-mock "0.1.5"] [ring/ring-devel "1.3.1"]]
    :env {:proto "postgresql" :subname "//localhost/photolog_dev"
          :db-user "developer" :password "a" 
          :log-path "logs/dev.log" :log-size 104857600
          :log-stdout true
          :log-backlog 5 :log-enabled true}}
   :test
   {:env {:proto "postgresql" :subname "//localhost/photolog_test"
          :db-user "developer" :password "a"
          :log-path "" :log-size 1
          :log-stdout false
          :log-backlog 0 :log-enabled false}}
   })
