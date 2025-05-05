(ns app.profile.edit.frontend
  (:require
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [lib.program :as p]
   [lib.ui.top-bar :as top-bar]))

(defn logic [_])

(defn view [i]
  (screen/view-screen
   i :screen/profile-edit
   [:div.w-full.h-full.flex.flex-col.items-center
    [top-bar/view {:top-bar/on-back #(p/put! i [:screen/clicked-link [:screen/profile]])
                   :top-bar/title "Edit Profile"}]
    [:h1 "Edit Profile"]]))

(mod/reg {:mod/name ::mod
          :mod/logic logic
          :mod/view view})