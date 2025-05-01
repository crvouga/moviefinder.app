(ns app.auth.session.entity
  (:require
   [clojure.spec.alpha :as s]
   [lib.time :as time]))

(s/def ::user-id string?)
(s/def ::session-id string?)
(s/def ::created-at inst?)
(s/def ::ended-at inst?)

(s/def ::session
  (s/keys :req [::user-id ::session-id ::created-at ::ended-at]))

(defn create [{:keys [session/user-id session/session-id]}]
  {:session/user-id user-id
   :session/session-id session-id
   :session/created-at (time/now!)})
