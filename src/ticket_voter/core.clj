(ns ticket-voter.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all])
  (:gen-class))

(def ticket-collection (atom (hash-map)))

(defn add-ticket-vote
  "Assigns given vote to the user-name and adds it to the list assigned to ticket-name keyword."
  [ticket-name user-name vote]
  (let [ticket-key (keyword ticket-name)]
    (swap! ticket-collection update-in [ticket-key]
           #(assoc-in % [(keyword user-name)] vote))))

(defn get-ticket-votes
  [ticket-name]
  (let [ticket-key (keyword ticket-name)]
    (if (keyword? ticket-key)
      (ticket-key (deref ticket-collection))
      nil)))

(defn request-ticket-votes
  [req]
  (let [ticket-name (:ticket-name (:params req))]
    (if (nil? ticket-name)
      {:status 400}
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (get-ticket-votes ticket-name)})))

(defn get-parameter [req pname] (get (:params req) pname))

(defn request-new-ticket-vote [req]
  (let [p (partial get-parameter req)
        ticket-name (p :ticket-name)
        user-name (p :user-name)
        vote (p :vote)]
    (if (some? (and ticket-name user-name vote))
      {:status  200
       :headers {"Content-Type" "text/html"}
       :body    (-> (let [p (partial get-parameter req)]
                      (add-ticket-vote (p :ticket-name) (p :user-name) (p :vote))
                      "Added!"))}
      {:status 400}
      ))
  )

(defroutes app-routes
           (GET "/vote" [] request-ticket-votes)
           (GET "/add-vote" [] request-new-ticket-vote)
           (POST "/add-vote" [] request-new-ticket-vote)
           (route/not-found "Error, page not found!"))

(defn -main
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; need to read about anti-forgery https://docs.microsoft.com/en-us/aspnet/web-api/overview/security/preventing-cross-site-request-forgery-csrf-attacks
    (server/run-server (wrap-defaults #'app-routes (assoc-in site-defaults [:security :anti-forgery] false))
                       {:port port})
    (println (str "Running webserver at http://127.0.0.1:" port "/"))))
