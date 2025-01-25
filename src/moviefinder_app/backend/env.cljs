(ns moviefinder-app.backend.env
  (:require [core.env :as env]))


(def port-env (env/get! "PORT"))

(when-not port-env
  (throw (js/Error. "PORT environment variable is not set")))

(def port (js/parseInt port-env))