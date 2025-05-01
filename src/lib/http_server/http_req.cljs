(ns lib.http-server.http-req
  (:require
   [clojure.core.async :refer [<! chan close! go go-loop put!]]
   [clojure.edn :as edn]
   [clojure.string :as s]))



(defn url
  "Returns the URL of the HTTP request without search parameters."
  [^js req]
  (let [full-url (.-url req)]
    (if-let [question-mark-idx (.indexOf full-url "?")]
      (.substring full-url 0 question-mark-idx)
      full-url)))

(defn body-binary-chan
  "Collects binary chunks from the HTTP request into a single binary sequence."
  [^js req]
  (let [body-chan (chan)]
    (.on req "data" #(put! body-chan %))
    (.on req "end" #(close! body-chan))
    (go-loop [chunks []]
      (if-let [chunk (<! body-chan)]
        (recur (conj chunks chunk))
        (.toString (js/Buffer.concat (clj->js chunks)))))))

(defn body-text-chan
  "Converts the binary body of the request into a text string."
  [^js req]
  (go
    (try
      (<! (body-binary-chan req))
      (catch :default e
        (js/console.error "Failed to read body text:" e)
        nil))))

(defn body-edn-chan
  "Parses the request body as EDN."
  [^js req]
  (go
    (try
      (let [body-text (<! (body-text-chan req))]
        (edn/read-string body-text))
      (catch :default e
        (js/console.error "Failed to parse EDN:" e)
        nil))))
(defn headers->map [^js req]
  (let [headers (.-headers req)
        entries (js->clj headers)]
    (reduce (fn [acc [k v]] (assoc acc k v)) {} entries)))

(defn get-header [^js req key]
  (get (headers->map req) key))

(defn get-cookie [^js req key]
  (let [cookie-header (get-header req "cookie")]
    (when cookie-header
      (let [cookies (s/split cookie-header #"; ")
            cookie-pairs (map #(s/split % #"=") cookies)
            cookie-map (reduce (fn [acc [k v]] (assoc acc k v)) {} cookie-pairs)]
        (get cookie-map key)))))
