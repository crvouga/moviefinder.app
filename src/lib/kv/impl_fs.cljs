(ns lib.kv.impl-fs
  (:require
   [lib.kv.inter :as kv]
   [clojure.core.async :as a]
   [lib.kv.shared :refer [assoc-ok to-namespaced-key]]
   [lib.fs.inter :as fs]
   [cljs.reader :as reader]
   [lib.pretty :as pretty]))

(def ^:private default-file-path "kv.edn")

(defn- read-store! [fs file-path]
  (a/go
    (let [exists? (a/<! (fs/file-exists? fs file-path))]
      (if exists?
        (let [content (a/<! (fs/read-file! fs file-path))]
          (try
            (reader/read-string content)
            (catch :default e
              (js/console.error "Failed to parse store:" e content)
              {})))
        {}))))

(defn- write-store! [fs file-path data]
  (a/go
    (a/<! (fs/write-file! fs file-path (pretty/str-edn data)))
    true))


(defmethod kv/init :kv/impl-fs
  [config]
  (let [file-path (or (:kv.impl-fs/file-path config) default-file-path)]
    (merge config
           {:kv.impl-fs/file-path file-path
            :kv.impl-fs/fs (or (:fs config) {:fs/impl :fs/node})})))

(defmethod kv/get! :kv/impl-fs
  [{:keys [kv.impl-fs/fs kv.impl-fs/file-path] :as inst} key]
  (a/go
    (let [store (a/<! (read-store! fs file-path))
          namespaced-key (to-namespaced-key inst key)
          value (get store namespaced-key)]
      (assoc-ok value))))

(defmethod kv/set! :kv/impl-fs
  [{:keys [kv.impl-fs/fs kv.impl-fs/file-path] :as inst} key value]
  (a/go
    (let [store (a/<! (read-store! fs file-path))
          namespaced-key (to-namespaced-key inst key)
          updated-store (assoc store namespaced-key value)]
      (a/<! (write-store! fs file-path updated-store))
      (assoc-ok value))))

(defmethod kv/zap! :kv/impl-fs
  [{:keys [kv.impl-fs/fs kv.impl-fs/file-path] :as inst} key]
  (a/go
    (let [store (a/<! (read-store! fs file-path))
          namespaced-key (to-namespaced-key inst key)
          updated-store (dissoc store namespaced-key)]
      (a/<! (write-store! fs file-path updated-store))
      (assoc-ok {}))))
