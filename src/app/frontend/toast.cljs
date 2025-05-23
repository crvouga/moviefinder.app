(ns app.frontend.toast)

(defn info [message]
  {:toast/id (str (random-uuid))
   :toast/variant :toast-variant/info
   :toast/message message
   :toast/duration 3000})

(defn error [message]
  {:toast/id (str (random-uuid))
   :toast/variant :toast-variant/error
   :toast/message message
   :toast/duration 3000})

(defn id [toast]
  (:toast/id toast))

(defn variant [toast]
  (or (:toast/variant toast) :toast-variant/info))

(defn message [toast]
  (:toast/message toast))

(defn duration [toast]
  (or (:toast/duration toast) 3000))
