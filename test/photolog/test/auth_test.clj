(ns photolog.test.auth-test
  (:use clojure.test
        photolog.handler
        peridot.core
        photolog.test.helpers))



(deftest test-auth
  (testing "good login"
    (with-user 
      (fn [response] 
        (is (= (get-in response [:response :status]) 200)))))

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
          (is (and (contains? resp :flash) (contains? (:flash resp) :error) (= (get-in resp [:flash :error]) "Nope") true))))))

  (testing "bad username"
    (avoid-csrf 
      (let [response 
        (-> (session app)
             (request "/gory" :request-method :post
                              :params {
                                    :user "bad"
                                    :pass "123123"
                                    }))]
        (let [resp (:response response)]
          (is (and (contains? resp :flash) (contains? (:flash resp) :error) (= (get-in resp [:flash :error]) "Nope") true)))))))
