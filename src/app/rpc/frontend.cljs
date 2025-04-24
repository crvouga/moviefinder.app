(ns app.rpc.frontend
  (:require
   [core.http-client :as http-client]
   [clojure.edn :as edn]
   [clojure.core.async :refer [<! go]]
   [core.program :as p]
   [app.frontend.config :refer [config]]))

(defn- rpc-fetch! [req]
  (http-client/fetch-chan!
   {:http-request/url (str (:wire/backend-url config) "/rpc")
    :http-request/method :http-method/post
    :http-request/headers {"Content-Type" "text/plain"}
    :http-request/body (pr-str req)}))

(defn rpc-res-chan! [req]
  (println "rpc-chan! " req)
  (go
    (let [res (<! (rpc-fetch! req))
          body (-> res :http-response/body)
          body-edn (-> body edn/read-string)
          ok? (-> res :http-response/ok?)]
      (if ok?
        body-edn
        (merge body-edn {:result/type :result/ok
                         :error/message "Errored while requesting from backend"})))))

(p/reg-eff
 :rpc/send!
 (fn [msg]
   (go
     (println "rpc/send! " msg)
     (let [res (<! (rpc-res-chan! (second msg)))]
       (println "rpc/send! res " res)
       res))))