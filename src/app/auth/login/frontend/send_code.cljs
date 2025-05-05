(ns app.auth.login.frontend.send-code
  (:require
   [app.frontend.mod :as mod]
   [app.frontend.screen :as screen]
   [app.frontend.toast :as toast]
   [clojure.core.async :as a]
   [lib.program :as p]
   [lib.result :as result]
   [lib.ui.button :as button]
   [lib.ui.text-field :as text-field]
   [lib.ui.top-bar :as top-bar]))

(defn- logic [i]
  (p/reg-reducer i ::set (fn [s [_ k v]] (assoc s k v)))

  (a/go-loop []
    (let [_ (a/<! (p/take! i ::form-submitted))]
      (p/put! i [::set ::request result/loading])
      (let [state (p/state! i)
            payload (-> state (select-keys [:verify-sms/phone-number]))
            res (a/<! (p/eff! i [:rpc/send! [:rpc/send-code payload]]))]
        (p/put! i [::set ::request res])
        (when (result/ok? res)
          (p/put! i [:screen/push [:screen/login-verify-code payload]])
          (p/put! i [:toaster/show (toast/info "Code sent")]))
        (when (result/err? res)
          (p/put! i [:toaster/show (toast/error (-> res :error/message))]))
        (recur)))))


(defn- loading? [i]
  (-> i ::request result/loading?))

(defn view-field-phone-number [i]
  [text-field/view
   {:text-field/label "Phone Number"
    :text-field/value (-> i :verify-sms/phone-number)
    :text-field/required? true
    :text-field/type :text-field/num-pad
    :text-field/disabled? (loading? i)
    :text-field/on-change #(p/put! i [::set :verify-sms/phone-number %])}])

(defn view-submit-button [i]
  [button/view
   {:button/type :button/submit
    :button/full? true
    :button/loading? (loading? i)
    :button/label "Send Code"}])

(defn- view-top-bar [i]
  [top-bar/view {:top-bar/title "Login with SMS"
                 :top-bar/on-back #(p/put! i [:screen/clicked-link [:screen/profile]])}])

(defn view-form [i & children]
  (vec
   (concat [:form.flex.flex-col.w-full.gap-6.p-6
            {:on-submit #(do (.preventDefault %) (p/put! i [::form-submitted]))}]
           children)))


(defn view [i]
  [screen/view-screen i :screen/login
   [view-top-bar i]
   [view-form i
    [view-field-phone-number i]
    [:div.w-full]
    [view-submit-button i]]])

(mod/reg
 {:mod/name ::mod
  :mod/view view
  :mod/logic logic})
