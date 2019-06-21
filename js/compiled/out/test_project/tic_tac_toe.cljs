(ns test-project.tic-tac-toe
  (:require [reagent.core :as reagent]
            [clojure.set :as s]))

(def initial-game-state {:board {1 nil
                                 2 nil
                                 3 nil
                                 4 nil
                                 5 nil
                                 6 nil
                                 7 nil
                                 8 nil
                                 9 nil}
                         :player :X
                         :winner nil})

(def game-state (reagent/atom initial-game-state))

(defn update-winner [value]
  (swap! game-state #(assoc % :winner value)))

(defn change-player! []
  (swap! game-state #(assoc % :player (if (= (:player %) :X) :O :X))))

(defn reset-game! []
  (reset! game-state initial-game-state))

(defn set-cell! [id]
  (swap! game-state #(assoc-in % [:board id] (:player %))))

(let [winning-sets [#{1 2 3}
                    #{4 5 6}
                    #{7 8 9}
                    #{1 4 7}
                    #{2 5 8}
                    #{3 6 9}
                    #{1 5 9}
                    #{3 5 7}]]
  (defn win? [moves]
    (some (fn [winning-set]
            (-> winning-set
                (s/intersection moves)
                (= winning-set)))
          winning-sets)))

(defn win-lose-draw [board player]
  (let [moves (reduce-kv (fn [prev k v]
                           (if (= v player)
                             (conj prev k)
                             prev))
                         #{}
                         board)
        player-win (when (win? moves) player)
        draw (when (every? identity (vals board)) :draw)]
    (or player-win draw)))


(defn cell-click [id]
  (when (and
          (nil? (:winner @game-state))
          (nil? (get (:board @game-state) id)))
    (set-cell! id)
    (when-let [result (win-lose-draw (:board @game-state) (:player @game-state))]
      (update-winner result))
    (change-player!)))

(defn button-cell [id value]
  [:div.button-cell {:class [value]
                     :on-click #(cell-click id)}
   [:div.flip {:class [(when value :flipped)]}
    [:div.cell-content
     (when value (name value))]]])

(defn flip-cell [id value]
  [:div.flip-container
   [:div.flip-inner
    [:div.flip-front]
    [:div.flip-back
     (when value (name value))]]])

(defn game-board [game-state]
  [:div.game-board
   (map (fn [[id value]]
          ^{:key id} [button-cell id value])
        (:board game-state))])

(defn tic-tac-toe []
  [:div.container.is-fluid
   [:h1.title "Tic-Tac-Toe"]
   [game-board @game-state]
   (when (:winner @game-state)
     [:div
      [:button.button {:on-click reset-game!} "Play Again"]
      [:p (if (= :draw (:winner @game-state))
            (str "Game ended in a draw!")
            (str "Winner is " (name (:winner @game-state))))]])])