(ns chat.views
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [chat.subs :as subs]
   [chat.events :as events]
   [chat.firebase :as fb]
   ))

(defn button [attrs text]
  [:button.button attrs text])

(defn sign-in []
  [:div.screen.sign-in
   [button {:on-click #(re-frame/dispatch [::events/sign-in])} "Sign In with GitHub"]])



(defn header [{:keys [left title right]}]
  [:div.header
   [:div.header-left left]
   [:div.header-title title]
   [:div.header-right right]])

(defn avatar [url size]
  [:img.avatar
   {:src (or url "https://user-images.githubusercontent.com/6009640/31679076-dc7581c6-b391-11e7-87fe-a8fa89793c63.png")}])

(defn message-input []
  [:div.footer
   [:textarea.input]
   [button {} "Send"]])

(defn content []
  [:div.content])

(defn chat [{:keys [id]}]
  (reagent/create-class
   {:component-did-mount
    (fn [] (fb/subscribe-room id #(re-frame/dispatch [::events/on-messages])))
    :reagent-render
    (fn [_]
      [:div.screen
       [header {:title "Clojure chat" :right [avatar]}]
       [content]
       [message-input]])}))

(defn me []
  [:div.screen.profile
   [header {:left [:small [:a {:href "#/chat"} "back to chat"]]
            :title "@username"}]
   [:div.content
    [:img.avatar.avatar-xl {:src "https://user-images.githubusercontent.com/6009640/31679076-dc7581c6-b391-11e7-87fe-a8fa89793c63.png"}]
    [:div.profile-info
     [:div.username "@johndoe"]
     [:div.full-name "John Doe"]
     [:div.bio "GitHub bio"]]
    [:button.button "Upload background"]]])

(defn user []
  [:div.screen.profile
   [:div.header
    [:div.header-left
     [:small [:a {:href "#/chat"} "back to chat"]]]
    [:div.header-title "@johndoe"]
    [:div.header-right]]
   [:div.content
    [:img.avatar.avatar-xl {:src "https://user-images.githubusercontent.com/6009640/31679076-dc7581c6-b391-11e7-87fe-a8fa89793c63.png"}]
    [:div.profile-info]
    [:div.username"@morda"]
    [:div.full-name "John Doe"]
    [:div.bio "GitHub bio"]]
   ])

(defn footer []
  [:div
   [:hr]
   [:a {:href "#/"} "sing in"]
   [:br]
   [:a {:href "#/me"} "my page"]
   [:br]
   [:a {:href "#/chat" :color "black"} "chat"]
   [:br]
   [:a {:href "#/users/12"} "other 12"]
   ])

(defn- routes [{:keys [route params]}]
  (js/console.log route)
  (case route
    :sign-in [sign-in]
    :chat [chat params]
    :me [me]
    :other [user]
    "404"
    ))


(defn main-panel []
  (let [current-route  @(re-frame/subscribe [::subs/route])]
    [:div
     [routes current-route]
     [footer]]
    ))
