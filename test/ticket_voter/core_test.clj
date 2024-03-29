(ns ticket-voter.core-test
  (:require [clojure.test :refer :all]
            [ticket-voter.core :refer :all]))

(deftest ticket-value-test
  (testing "overwrites vote value from the same user"
    (let [ticket-name "ABC-234"
          ticket-id (keyword ticket-name)
          user-name "mja"
          ticket-vote 2]
      (add-ticket-vote ticket-name user-name 1)
      (is (= {(keyword user-name) ticket-vote}
             (ticket-id (add-ticket-vote ticket-name user-name ticket-vote))))))

  (testing "allows multiple votes for the same ticket"
    (let [ticket-name "ABC-345"
          ticket-id (keyword ticket-name)]
      (add-ticket-vote ticket-name "John" 1)
      (is (= {(keyword "John") 1, (keyword "Alice") 2}
             (ticket-id (add-ticket-vote ticket-name "Alice" 2))))))

  (testing "allows getting current value"
    (let [ticket-name "ABC-456"
          user-name "Alice"
          ticket-value 2]
      (add-ticket-vote ticket-name user-name ticket-value)
      (is (= {(keyword user-name) ticket-value}
             (get-ticket-votes ticket-name)))
      (is (= nil
             (get-ticket-votes "other")))))

  (testing "returns nil for integer ticket name"
    (is (= nil (get-ticket-votes 1))))
  )

(deftest routes-requests-test
  (testing "returns 400 for missing param on getting a ticket"
    (is (= {:status 400}
           (request-ticket-votes nil))))

  (testing "returns 400 for missing param on adding a vote"
    (is (= {:status 400}
           (request-new-ticket-vote {:params {:ticket-name "A-1" :user-name "me"}})))
    (is (= {:status 400}
           (request-new-ticket-vote {:params {:user-name "me" :vote 1}})))
    (is (= {:status 400}
           (request-new-ticket-vote {:params {:ticket-name "A-1" :vote 1}}))))

  (testing "gets added vote"
    (request-new-ticket-vote {:params {:ticket-name "A-1", :user-name "me", :vote 1}})
    (is (= {:status 200 :headers {"Content-Type" "text/html"} :body {:me 1}}
           (request-ticket-votes {:params {:ticket-name "A-1"}})))))