(ns moviefinder-app.auth.login.frontend.shared
  (:require
   [moviefinder-app.frontend.ui.top-bar :as top-bar]))

(defn view-layout [title body]
  [:main.w-full
   [top-bar/view {:top-bar/title title}]
   [:section.p-6.w-full body]])