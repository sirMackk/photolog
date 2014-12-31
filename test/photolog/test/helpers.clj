(ns photolog.test.helpers
  (:use clojure.test
        ;ring.mock.request
        photolog.handler
        peridot.core))

(defn fake-csrf [req token]
  true)

(defmacro avoid-csrf [body]
  `(with-redefs [ring.middleware.anti-forgery/valid-request? fake-csrf]
     ~body))

(defmacro with-user [test & interactions]
  `(avoid-csrf
    (let [response# 
      (-> (session app)
          (request "/gory" :request-method :post
                           :params {
                                  :user "matt"
                                  :pass "123123"
                                  })
          (follow-redirect)
          ~@interactions)]
      (~test response#))))
