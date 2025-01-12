(ns linkhub.backend.env
  (:require ["fs" :as fs]
            ["path" :as path]
            [clojure.string]))

(defn load-env []
  (let [env-path (.resolve path ".env")
        exists? (.existsSync fs env-path)]
    (when exists?
      (let [content (.readFileSync fs env-path "utf8")]
        (doseq [line (clojure.string/split content #"\n")]
          (let [[key value] (clojure.string/split line #"=" 2)]
            (when (and key value)
              (aset js/process.env (clojure.string/trim key) (clojure.string/trim value)))))))))

(load-env)

(def port-env (-> js/process.env .-PORT))

(when-not port-env
  (throw (js/Error. "PORT environment variable is not set")))

(def port (js/parseInt port-env))