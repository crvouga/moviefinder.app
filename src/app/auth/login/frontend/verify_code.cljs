(ns app.auth.login.frontend.verify-code
  (:require
   [app.frontend.screen :as screen]
   [app.frontend.toast :as toast]
   [clojure.core.async :refer [<! go-loop]]
   [core.program :as program]
   [core.result :as result]
   [core.ui.button :as button]
   [core.ui.text-field :as text-field]
   [core.ui.top-bar :as top-bar]))


(defn flow-submit-form! [{:keys [eff! put! take! write! read!]}]
  (go-loop []
    (let [_ (<! (take! ::user-submitted-form))

          _ (write! assoc ::request result/loading)

          code (-> (read!) ::code)

          phone-number (-> (read!) ::screen second :user/phone-number)

          req [:rpc/verify-code
               {:user/phone-number phone-number
                :verify-sms/code code}]

          res (<! (eff! :rpc/send! req))

          _ (write! assoc ::request res)

          _ (when (result/ok? res)
              (put! [:login/authenticated res])
              (put! [:screen/push [:screen/profile]])
              (put! [:toaster/show (toast/info "Logged in")]))

          _ (when (result/error? res)
              (put! [:toaster/show (toast/error "Failed to log in")]))]

      (recur))))

(defn flow-input-code! [{:keys [take! write!]}]
  (go-loop []
    (let [msg (<! (take! ::inputted-code))]
      (write! assoc ::code (-> msg second ::code))
      (recur))))

(program/extend! flow-input-code! flow-submit-form!)


(defn loading? [i]
  (-> i :store/state ::request result/loading?))

(screen/register!
 :screen/login-verify-code
 (fn [{:keys [read! put!]}]
   [:div.w-full.flex-1
    [top-bar/view {:top-bar/title "Verify Code"
                   :top-bar/on-back #(put! [:screen/clicked-link [:screen/login]])}]
    [:form.flex.flex-col.w-full.gap-6.p-6
     {:on-submit #(do (.preventDefault %) (put! [::user-submitted-form]))}
     [:p.text-lg "Enter the code we sent to " [:span.font-bold (-> (read!) ::screen second :user/phone-number)]]
     [text-field/view
      {:text-field/label "Code"
       :text-field/value (-> (read!) ::code)
       :text-field/type :text-field-type/number-pad
       :text-field/required? true
       :text-field/disabled? (loading? read!)
       :text-field/on-change #(put! [::inputted-code %])}]
     [:div.w-full]
     [button/view
      {:button/type :button-type/submit
       :button/loading? (loading? read!)
       :button/full? true
       :button/label "Verify Code"}]]]))