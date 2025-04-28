(ns app.auth.user-session
  (:require [clojure.spec.alpha :as s]))

(s/def ::user-id string?)
(s/def ::session-id string?)
(s/def ::created-at inst?)
(s/def ::ended-at inst?)

(s/def ::user-session
  (s/keys :req [::user-id ::session-id ::created-at ::ended-at]))
