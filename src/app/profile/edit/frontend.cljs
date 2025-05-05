(ns app.profile.edit.frontend
  (:require
   [app.auth.current-user.frontend :as current-user]
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.profile.avatar-url :as avatar-url]
   [clojure.core.async :as a]
   [clojure.set :refer [rename-keys]]
   [lib.map-ext :as map-ext]
   [lib.program :as p]
   [lib.result :as result]
   [lib.ui.avatar :as avatar]
   [lib.ui.confirmation :as confirmation]
   [lib.ui.form :as form]
   [lib.ui.text-field :as text-field]
   [lib.ui.top-bar :as top-bar]
   [app.frontend.toast :as toast]
   [lib.err :as err]))



(def kmap-user->form {:user/username ::username
                      :user/fullname ::fullname
                      :user/avatar-seed ::avatar-seed})
(def user-keys (keys kmap-user->form))
(def kmap-form->user (map-ext/inverse kmap-user->form))


(defn logic [i]
  (p/reg-reducer i ::set (fn [s [_ k v]] (assoc s k v)))
  (p/reg-reducer i ::reset (fn [s _] (-> s current-user/to-current-user (rename-keys kmap-user->form))))

  (screen/take-every! i :screen/profile-edit (fn [] (p/put! i [::reset])))
  (p/take-every! i :current-user/loaded (fn [] (p/put! i [::reset])))
  (p/take-every! i ::cancel (fn [] (p/put! i [:screen/push [:screen/profile]])))

  (a/go-loop []
    (let [_ (a/<! (p/take! i ::form-submitted))]

      (p/put! i [::set ::request result/loading])

      (let [edits (-> i p/state! (rename-keys kmap-form->user) (select-keys user-keys))
            res (a/<! (p/eff! i [:rpc/send! [:profile-edit/rpc edits]]))]

        (p/put! i [::set ::request res])

        (when (result/err? res)
          (p/put! i [:toaster/show (toast/error (err/message res))]))

        (when (result/ok? res)
          (p/put! i [:current-user/edit edits])
          (p/put! i [:current-user/load])
          (p/put! i [:screen/push [:screen/profile]])
          (p/put! i [:toaster/show (toast/info "Profile updated")]))

        (recur)))))

(defn view-section [& children]
  (vec (concat [:div.w-full.flex.flex-col.items-center.justify-center.p-6.gap-3] children)))

(defn loading? [i]
  (-> i ::request result/loading?))

(defn view-edit-avatar-section [i]
  [view-section
   [avatar/view {:avatar/size 100
                 :avatar/loading? (-> i current-user/loading?)
                 :avatar/src (-> i ::avatar-seed avatar-url/to)}]
   [text-field/view
    {:text-field/class "w-full"
     :text-field/disabled (loading? i)
     :text-field/label "Avatar Seed"
     :text-field/on-change #(p/put! i [::set ::avatar-seed %])
     :text-field/value (-> i ::avatar-seed)}]])

(defn view-edit-username-section [i]
  [view-section
   [text-field/view
    {:text-field/class "w-full"
     :text-field/disabled (loading? i)
     :text-field/label "Username"
     :text-field/on-change #(p/put! i [::set ::username %])
     :text-field/value (-> i ::username)}]])

(defn view-edit-fullname-section [i]
  [view-section
   [text-field/view
    {:text-field/class "w-full"
     :text-field/disabled (loading? i)
     :text-field/label "Fullname"
     :text-field/on-change #(p/put! i [::set ::fullname %])
     :text-field/value (-> i ::fullname)}]])



(defn view [i]
  (screen/view-screen
   i :screen/profile-edit
   [:div.w-full.h-full.flex.flex-col.items-center
    [top-bar/view {:top-bar/on-back #(p/put! i [::cancel])
                   :top-bar/title "Edit Profile"}]
    [form/view {:class "w-full flex-col gap-6 overflow-y-scroll flex-1"
                :form/on-submit #(p/put! i [::form-submitted])}
     [view-edit-avatar-section i]
     [view-edit-username-section i]
     [view-edit-fullname-section i]
     [:div.w-full.h-96]
     [:div.absolute.bottom-0.left-0.right-0.p-4.bg-black
      [confirmation/view-buttons
       {:confirmation/cancel-text "Cancel"
        :confirmation/on-cancel #(p/put! i [::cancel])
        :confirmation/confirm-loading? (loading? i)
        :confirmation/confirm-text "Edit"}]]]]))

(mod/reg {:mod/name ::mod
          :mod/logic logic
          :mod/view view})