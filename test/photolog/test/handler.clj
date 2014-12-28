(ns photolog.test.handler
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



(deftest test-app
  ;(testing "main route"
    ;(let [response (app (request :get "/"))]
      ;(is (= (:status response) 200))
      ;(is (= (:body response) "Hello World"))))

  ;(testing "not-found route"
    ;(let [response (app (request :get "/invalid"))]
      ;(is (= (:status response) 404))))
  ;
  (testing "good login"
    (with-user (fn [response] (is (= (get-in response [:response :status]) 200)))))

  (testing "bad password"
    (avoid-csrf 
      (let [response 
        (-> (session app)
             (request "/gory" :request-method :post
                              :params {
                                    :user "matt"
                                    :pass "nope"
                                    }))]
        (let [resp (:response response)]
          (is (and (contains? resp :flash) (contains? (:flash resp) :error) (= (get-in resp [:flash :error]) "Nope") true)))))))


        ;)))
      ;app :headers (get "Set-Cookie") not-empty))
