(ns linkpage.frontend.route
  (:require [cljs.reader :refer [read-string]]))

(defn encode [route]
  (-> route pr-str js/btoa))

(defn decode [route]
  (-> route js/atob read-string))

(defn get! []
  (-> js/window.location.pathname (subs 1) decode))
