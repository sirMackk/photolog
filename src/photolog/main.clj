(ns photolog.main
  (:use photolog.handler
         [org.httpkit.server :only [run-server]]
         [ring.middleware file file-info])
  (:gen-class))

(defn -main [& [port]]
  (let [port (if port (Integer/parseInt port) 3000)]
    (println "Running!")
    (run-server app {:port port})))
