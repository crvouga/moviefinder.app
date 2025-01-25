(ns moviefinder-app.auth.login.frontend.shared
  (:require
   [core.ui.top-bar :as top-bar]))

(defn view-layout [title body]
  [:main.w-full.flex-1
   [top-bar/view {:top-bar/title title}]
   [:section.p-6.w-full body]])