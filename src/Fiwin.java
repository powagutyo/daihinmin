import static jp.ac.uec.daihinmin.card.MeldFactory.createSingleMeldJoker;
import jp.ac.uec.daihinmin.card.*;
import jp.ac.uec.daihinmin.player.*;
import jp.ac.uec.daihinmin.*;
//import javax.swing.JOptionPane;

public class Fiwin extends BotSkeleton {
       /**
        * playedCards[suitNum][rankNum]
        * suitNum=0 →　SPADES suitNum=1　→　HEARTS suitNum=2　→　CLUBS suitNum=3　→　DIAMONDS
        */

       private boolean playedCardsCheck;
       /**
        * パスしたプレイヤ
        */
       private boolean[] playerPassed;
       /**
        * 既に勝利したプレイヤ
        */
       private boolean[] playerWon;
       /**
        * 場に出たカード
        */
       private boolean[][] playedCards;

       /**
        * 現在が通常か革命中か。trueは通常。false は革命中。
        */
       public boolean order;
       /**
        * 現在組める役集合
        */
       public Melds melds;

       Extractor<Meld,Melds> MaxSize = Melds.MAX_SIZE;

       @Override
       public Cards requestingGivingCards() {
    	   try {
               Cards myHand = this.hand(); // 自分の手札を取得
               // 手札を昇順にソート．たとえば，D3 D4 ... HA S2 JOKER
               Cards sortedHand = Cards.sort(myHand);
               Rules rules = this.rules(); // ルールを取得
               int rank = this.rank(); // 自分のランク(貧民など)を取得
               int heiminRank = Rules.heiminRank(rules); // 平民のランクを取得
               int exchanges = Rules.sizeGivenCards(rules, rank); // 交換枚数

               Cards result = Cards.EMPTY_CARDS; // 結果を格納する変数を空にする
               // 交換枚数だけ，result にカードを追加
               for(int i = 0; i < exchanges; i++){
                       if(heiminRank < rank) {
                               // 貧民か大貧民は、高いカードを選択
                               result = result.add(sortedHand.get(i));
                       } else {
                               // 富豪か大富豪は、低いカードを選択
                               //JOKER,S3を除く
                               sortedHand = TradeJOKER(sortedHand);
                               // Group 型の対象カードを削除
                               Melds groupMelds = Melds.parseGroupMelds(sortedHand);
                               for(Meld meld: groupMelds) {
                                       sortedHand = sortedHand.remove(meld.asCards());
                               }
                               // Sequence 型の対象カードを削除
                               Melds seqMelds = Melds.parseSequenceMelds(sortedHand);
                               for(Meld meld: seqMelds) {
                                       sortedHand = sortedHand.remove(meld.asCards());
                               }

                               //役を持つカードを除外D3,8など
                               sortedHand = removeTrade(sortedHand);

                               //交換カードが足りない時1　⇒ ペアを外す
                               // 藤田: for 文で回しているので、exchanges との比較ではなくて、1との比較で良いはず
                               //修正11/5　exchanges から 1　に変更
                               if(sortedHand.size() >= 1){
                                       //System.out.println("■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■■");
                                       result = result.add(sortedHand.get(0));
                               }else{
                                       //System.out.println("□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□□");
                                       sortedHand = Cards.sort(myHand);
                                       sortedHand = TradeJOKER(sortedHand);
                                       // Sequence 型の対象カードを削除
                                       //for(Meld meld: seqMelds) {
                                       //      sortedHand = sortedHand.remove(meld.asCards());
                                       //}
                                       for(Meld meld: groupMelds) {
                                               sortedHand = sortedHand.remove(meld.asCards());
                                       }
                                       sortedHand = removeTrade(sortedHand);

                                       //まだ足りないなら階段を外す
                                       // 藤田: for 文で回しているので、exchanges との比較ではなくて、1との比較で良いはず
                                       //修正11/5　exchanges から 1　に変更
                                       if(sortedHand.size() >= 1){
                                               result = result.add(sortedHand.get(0));
                                       }else{
                                               sortedHand = Cards.sort(myHand);
                                               sortedHand = TradeJOKER(sortedHand);
                                               sortedHand = removeTrade(sortedHand);
                                               result = result.add(sortedHand.get(0));
                                       }
                                       //階段多い場合
                               }
                       }
               }
               // たとえば，大貧民なら JOKER S2
               // たとえば，大富豪なら D3 D4
               return result;
    	   } catch(Exception e) {
    		   // 藤田: Exception が出た時の対応を追加
    		   Cards result = Cards.EMPTY_CARDS;
    		   
    		   Cards sortedHand = Cards.sort(this.hand());
    		   sortedHand = sortedHand.remove(Card.JOKER);
    		   int givenSize = Rules.sizeGivenCards(this.rules(),this.rank());
    		   
    		   result.add(sortedHand.get(0));
    		   if(givenSize == 2) {
    			   result.add(sortedHand.get(1));
    		   }
    		   
    		   return result;
    	   }
       }

       @Override
       public Meld requestingPlay(){
               try {
               //手札を残りカードから除外
               if(playedCardsCheck == false){
                       myCardsRemove();
               }

               Place place = this.place();
               Melds melds;
               Melds MaxMelds;
               Cards cards = this.hand();
               Cards sortedHand = Cards.sort(cards);
               Cards irregular = Cards.EMPTY_CARDS;
               //誤った手札が取得されている？
               //もし同じカードが手札にある場合はそれを記憶し、削除する
               for(int i = 0;i < sortedHand.size()-1; i++){
                       //System.out.println("手" + sortedHand.get(i));
                       if(sortedHand.get(i) == sortedHand.get(i+1)){
                               //System.out.println("手札に同じカードが存在します。");
                               //JOptionPane.showMessageDialog(null, "おかしい！");
                               irregular = irregular.add(sortedHand.get(i));
                       }
               }
               if(irregular.isEmpty() == false){
                       for(Card card:irregular){
                               sortedHand = sortedHand.remove(card);
                       }
               }

               int count = 0;
               int winHandCount = 0;
               int myJOKER = 0;
               order = place.order()== Order.NORMAL;

               //最強手をそれぞれのタイプに分けて格納(予定)
               Cards singleWin = Cards.EMPTY_CARDS;
               Melds groupWin = Melds.EMPTY_MELDS;
               Melds seqWin = Melds.EMPTY_MELDS;
               Melds MaxSize_Rank = Melds.EMPTY_MELDS;
               Melds winMelds = Melds.EMPTY_MELDS;
               Melds testMelds = Melds.EMPTY_MELDS;
               Cards loseCards = Cards.EMPTY_CARDS;
               Melds loseMelds = Melds.EMPTY_MELDS;
               Card loseCard = null;

               //グループ優先
               if(sortedHand.contains(Card.JOKER) == true){
                       myJOKER = 1;
                       sortedHand = sortedHand.remove(Card.JOKER);
               }

               Melds groupMelds = Melds.parseGroupMelds(sortedHand);
               // Group 型の対象カードを削除
               for(Meld meld: groupMelds) {
                       sortedHand = sortedHand.remove(meld.asCards());
               }
               Melds seqMelds = Melds.parseSequenceMelds(sortedHand);
               // Sequence 型の対象カードを削除
               for(Meld meld: seqMelds) {
                       sortedHand = sortedHand.remove(meld.asCards());
               }
               Cards single = sortedHand;

               Melds testHand = Melds.EMPTY_MELDS;

               //シングルでの最強手判定
               for(Card card: sortedHand){
                       //System.out.println("シングルカードの強弱を判定します");
                       if(isStrongestCard(card,order) == false){
                               //JOKERを持っているならペアにして最強かどうかを判定してみる
                               if(myJOKER == 1){
                                       Cards testCard = Cards.EMPTY_CARDS;
                                       testCard = testCard.add(Card.JOKER);
                                       testCard = testCard.add(card);
                                       testHand = Melds.parseGroupMelds(testCard);
                                       if(isStrongestMeld(testHand.get(0),order) == true){
                                               myJOKER = 0;
                                               groupWin = groupWin.add(testHand.get(0));
                                               winHandCount++;
                                               //cards.remove(meld.asCards());
                                       }else{
                                               loseCard = card;
                                               count++;
                                       }
                               }else{
                                       loseCard = card;
                                       count++;
                               }
                       }else{
                               singleWin = singleWin.add(card);
                               winHandCount++;
                               //cards.remove(card);
                       }
                       if(count >= 2){
                               //必勝手不可
                               break;
                       }
               }

               Cards testCards;
               //グループでの最強手判定
               if(groupMelds.isEmpty() == false && count < 2){
                       //System.out.println("グループカードの強弱を判定します");
                       testCards = Cards.EMPTY_CARDS;
                       for(Meld meld:groupMelds){
                               for(Card card: meld.asCards()){
                                       if(!testCards.contains(card)){
                                               testCards = testCards.add(card);
                                       }
                               }
                       }
                       while(testCards.isEmpty() == false){
                               if(count >= 2){
                                       //必勝手不可
                                       break;
                               }
                               testMelds = Melds.parseGroupMelds(testCards);
                               MaxMelds = testMelds.extract(Melds.MAX_SIZE);
                               if(order){
                                       MaxSize_Rank = MaxMelds.extract(Melds.MAX_RANK);
                               }else{
                                       MaxSize_Rank = MaxMelds.extract(Melds.MIN_RANK);
                               }
                               for(Meld meld: MaxSize_Rank){
                                       //Card check = meld.asCards().get(0);
                                       if(isStrongestMeld(meld, order) == false){
                                               if(myJOKER == 1){
                                                       Cards testCard = Cards.EMPTY_CARDS;
                                                       testCard = testCard.add(meld.asCards());
                                                       testCard = testCard.add(Card.JOKER);
                                                       testHand = Melds.parseGroupMelds(testCard);
                                                       Melds MaxM = testHand.extract(Melds.MAX_SIZE);
                                                       for(Meld meld2: MaxM){
                                                               if(isStrongestMeld(meld2,order) == true){
                                                                       myJOKER = 0;
                                                                       groupWin = groupWin.add(meld2);
                                                                       testCards = testCards.remove(meld2.asCards());
                                                                       winHandCount++;
                                                               }else{
                                                                       count++;
                                                                       if(count >= 2){break;}
                                                                       testCards = testCards.remove(meld2.asCards());
                                                                       loseMelds = loseMelds.add(meld);
                                                               }
                                                       }
                                               }else{
                                                       count++;
                                                       if(count >= 2){break;}
                                                       loseMelds = loseMelds.add(meld);
                                                       testCards = testCards.remove(meld.asCards());
                                               }
                                       }else{
                                               groupWin = groupWin.add(meld);
                                               testCards = testCards.remove(meld.asCards());
                                               winHandCount++;
                                       }
                               }
                               //System.out.println("グループ：while内部part3");
                       }
               }

//              //階段での最強手判定
               if(seqMelds.isEmpty() == false && count <= 2){
                       //System.out.println("階段カードの強弱を判定します");
                       testCards = Cards.EMPTY_CARDS;;
                       for(Meld meld:seqMelds){
                               for(Card card: meld.asCards()){
                                       if(!testCards.contains(card)){
                                               testCards = testCards.add(card);
                                       }
                               }
                       }
                       while(testCards.isEmpty() == false){
                               if(count >= 2){
                                       //必勝手不可
                                       break;
                               }
                               testMelds = Melds.EMPTY_MELDS;
                               testMelds = Melds.parseSequenceMelds(testCards);
                               MaxMelds = testMelds.extract(Melds.MAX_SIZE);

                               if(order){
                                       MaxSize_Rank = MaxMelds.extract(Melds.MAX_RANK);
                               }else{
                                       MaxSize_Rank = MaxMelds.extract(Melds.MIN_RANK);
                               }

                               for(Meld meld: MaxSize_Rank){
                                       if(isStrongestMeld(meld, order) == false){
                                               if(myJOKER == 1){
                                                       Cards testCard = Cards.EMPTY_CARDS;
                                                       testCard = testCard.add(meld.asCards());
                                                       testCard = testCard.add(Card.JOKER);
                                                       testHand = Melds.parseSequenceMelds(testCard);
                                                       Melds MaxM = testHand.extract(Melds.MAX_SIZE);
                                                       //JOKERを変化させた時、大きい方を取る
                                                       MaxM = MaxM.extract(order? Melds.MAX_RANK:Melds.MIN_RANK);
                                                       if(isStrongestMeld(MaxM.get(0),order) == true){
                                                               myJOKER = 0;
                                                               seqWin = seqWin.add(MaxM.get(0));
                                                               testCards = testCards.remove(MaxM.get(0).asCards());
                                                               winHandCount++;
                                                       }else{
                                                               count++;
                                                               if(count >= 2){break;}
                                                               testCards = testCards.remove(MaxM.get(0).asCards());
                                                               loseMelds = loseMelds.add(meld);
                                                       }
                                               }else{
                                                       count++;
                                                       if(count >= 2){break;}
                                                       testCards = testCards.remove(meld.asCards());
                                                       loseMelds = loseMelds.add(meld);
                                               }
                                       }else{
                                               seqWin = seqWin.add(meld);
                                               testCards = testCards.remove(meld.asCards());
                                               winHandCount++;
                                       }
                               }
                       }
               }

//              //返される手が2手未満　⇒　必勝手稼働
               if(count < 2){
                       winMelds = winMelds.add(groupWin);
                       winMelds = winMelds.add(seqWin);

                       Melds sinWin = Melds.parseMelds(singleWin);
                       winMelds = winMelds.add(sinWin);

                       //Melds subWin = winMelds;
                       if(loseCard != null){
                               loseCards = loseCards.add(loseCard);
                               loseMelds = loseMelds.add(Melds.parseMelds(loseCards));
                       }

                       //場が流れたか否か
                       if(place.isRenew() == true){//JOKERの判断
                               //どれでもいい　が、空かどうか
                               winMelds = winMelds.extract(Melds.MAX_SIZE);
                               if(order){
                                       winMelds = winMelds.extract(Melds.MIN_RANK);
                               }else{
                                       winMelds = winMelds.extract(Melds.MAX_RANK);
                               }
                               if(winMelds.isEmpty() == false){
                                       return winMelds.get(0);
                               }
                       }else{
                               //場にカードがあり、候補にそれを返せるカードがあるかどうか
                               if(!place.lockedSuits().equals(Suits.EMPTY_SUITS)){
                               winMelds = winMelds.extract(Melds.suitsOf(place.lockedSuits()));
                       }
                               winMelds = adjustSizeType(winMelds);
                               winMelds = adjustRank(winMelds);

                               if(winMelds.isEmpty() == false){
                                       // 藤田: loseMelds と winMelds は排他的なはずであり、ここの論理はおかしくない？
                                       // ここの if 文は、不要なように思うのですが。
                            	   		//修正11/5　コメント化
//                                       if(loseMelds.isEmpty() == false && winHandCount < 2){
//                                               winMelds = winMelds.remove(loseMelds.get(0));
//                                               if(winMelds.isEmpty() == true){
//                                                       return loseMelds.get(0);
//                                               }
//                                       }
                                       if(order){
                                               winMelds = winMelds.extract(Melds.MIN_RANK);
                                       }else{
                                               winMelds = winMelds.extract(Melds.MAX_RANK);
                                       }
                                       if(winMelds.isEmpty() == false){
                                               return winMelds.get(0);
                                       }
                               }
                       }
               }

               //***************************************************************************************
               //System.out.println("必勝手は実行できません");
               melds = Melds.parseMelds(this.hand());
               Melds singleMelds = Melds.parseMelds(single);
               //自分以外全員パスなら場を流す
               if(isOtherPlayerAllPassed() == true){
                       if(!place.lockedSuits().equals(Suits.EMPTY_SUITS) && place.size() == 1){
                               singleMelds = singleMelds.extract(Melds.suitsOf(place.lockedSuits()));
                               singleMelds = Melds.sort(singleMelds);
                               // 藤田: これだと、ランクの低い singleMelds も場に堕してしまう。
                               // また、order を考慮していないので、革命時にうまく動作しない
                               //修正：11/5 ランクを考慮
                               singleMelds = adjustRank(singleMelds);
                               if(singleMelds.isEmpty() == false){
                                       return singleMelds.get(0);
                               }
                       }
                       return MeldFactory.PASS;
               }

               melds = Melds.parseMelds(this.hand());
               //場が流れた直後か
               if(place.isRenew()) {
                       return createRenewMeld();
               }

               if(this.place().hasJoker() == true && this.place().size() == 1 && this.hand().contains(Card.S3) == true){
                       Meld meldS3 = MeldFactory.createSingleMeld(Card.S3);
                       melds = Melds.EMPTY_MELDS;
                       //melds = melds.add(meldS3);
                       return meldS3;
               }

               // サイズが1の時に、ペアや階段のカードを対象外にする
               // ただし、Ace や 2 は、バラバラで出す。
               if(this.place().size() == 1) {
                       melds = selectSingleCandidates();
               } else {
                       melds = Melds.parseMelds(this.hand());
               }

               //if()

               //場が縛られていれば
       if(!place.lockedSuits().equals(Suits.EMPTY_SUITS)){
               //場を縛っているスート集合に適合する役を抽出して,候補とする．
               melds = melds.extract(Melds.suitsOf(place.lockedSuits()));
       }

       // サイズとタイプの合った役集合を抽出
       melds = adjustSizeType(melds);

       // ランクの合った役集合を抽出
       melds = adjustRank(melds);

               if(melds.isEmpty()) {
                       cards = this.hand();
                       // 残り5枚まではJOKERを単騎で使わない
                       if(place.size() == 1 && cards.size() < 5 && cards.contains(Card.JOKER)) {
                               return createJokerMeld();
                       } else {
                               return MeldFactory.PASS;
                       }
               } else {
                       //JOptionPane.showMessageDialog(null, melds);
                       return checkJoker(melds.get(0));
               }
               } catch (Exception e) {
                       // 藤田: Exception が出た時の対処
                       return MeldFactory.PASS;
               }
       }

       Melds selectSingleCandidates() {
               Cards cards = this.hand();
               Cards strongerCards = Cards.EMPTY_CARDS;
               for(Card card: cards) {
                       if(card == Card.JOKER) {
                               // JOKER は加えない
                               // strongerCards = strongerCards.add(card);
                       } else if(this.place().order() == Order.NORMAL && card.rank().toInt() > 13) {
                               strongerCards = strongerCards.add(card);
                       } else if(this.place().order() == Order.REVERSED && card.rank().toInt() < 5) {
                               strongerCards = strongerCards.add(card);
                       }
               }

               cards = singleCards(cards);
               for(Card card: strongerCards) {
                       if(!cards.contains(card)) {
                               cards = cards.add(card);
                       }
               }
               // 藤田: strongerCards には、Aや2が複数枚含まれる場合があるので、cards 集合から parseMelds すると
               // グループや階段が含まれる場合がある
               //修正11/5　グループ・ペアを除外
               Melds singleOnly = Melds.EMPTY_MELDS;
               singleOnly = Melds.parseMelds(cards);
               for(Meld meld:singleOnly){
            	   if(meld.type() == Meld.Type.GROUP){
            		   singleOnly = singleOnly.remove(meld);
            	   }else if(meld.type() == Meld.Type.SEQUENCE){
            		   singleOnly = singleOnly.remove(meld);
            	   }
               }
               return singleOnly;
       }


       /**
        * 場に出せるタイプとサイズに合わせた役集合に絞り込む
        * @param melds 役集合の候補
        * @return 絞り込んだ役集合
        */
       Melds adjustSizeType(Melds melds) {
               Extractor<Meld, Melds> sizeExtractor = Melds.sizeOf(this.place().size());
               Extractor<Meld, Melds> typeExtractor = Melds.typeOf(this.place().type());
               return melds.extract(sizeExtractor.and(typeExtractor));
       }

       /**
        * 場に出せるランクに合わせた役集合に絞り込む
        * @param melds 役集合の候補
        * @return 絞り込んだ役集合
        */
       Melds adjustRank(Melds melds) {
               // 革命を考慮しながら、出せるランクのカードに絞り込む
               Rank rank = this.place().rank(); // 現在の場のランク
               
               //修正11/6　場のカードが階段の場合は階段のサイズをランクに加算
               int addRank;
               if(this.place().type() == Meld.Type.SEQUENCE){
//            	   System.out.println("場のランク："+rank);
            	   addRank = this.place().size();
//           	   System.out.println("場のサイズ："+ addRank);
            	   rank = rank.higher(addRank-1);
//            	   System.out.println("変更後の場のランク："+ rank);
               }
               Extractor<Meld, Melds> rankExtractor;
               if(this.place().order() == Order.NORMAL) {
                       // JOKER が出ている時は、勝てないのであきらめる
                       if(rank != Rank.JOKER_HIGHEST) {
                               Rank oneRankHigh = rank.higher();
                               rankExtractor = Melds.rankOf(oneRankHigh).or(Melds.rankOver(oneRankHigh));
                               melds = melds.extract(rankExtractor);
                               melds = melds.extract(Melds.MIN_RANK);
                       } else if(this.hand().contains(Card.S3) && this.place().size() == 1 ) {
                    	   //修正11/6　場のサイズが2以上のときもS3単騎で出そうとしているので修正
                               // スペードの3を出す
                               Meld meld = MeldFactory.createSingleMeld(Card.S3);
                               melds = Melds.EMPTY_MELDS;
                               return melds.add(meld);
                       } else {
                               melds = Melds.EMPTY_MELDS;
                       }
               } else {
                       if(rank != Rank.JOKER_LOWEST) {
                               Rank oneRankHigh = rank.lower();
                               rankExtractor = Melds.rankOf(oneRankHigh).or(Melds.rankUnder(oneRankHigh));
                               melds = melds.extract(rankExtractor);
                               melds = melds.extract(Melds.MAX_RANK);
                       } else if(this.hand().contains(Card.S3) && this.place().size() == 1 ) {
                    	   //修正11/6　場のサイズが2以上のときもS3単騎で出そうとしているので修正
                               // スペードの3を出す
                               Meld meld = MeldFactory.createSingleMeld(Card.S3);
                               melds = Melds.EMPTY_MELDS;
                               return melds.add(meld);
                       } else {
                               melds = Melds.EMPTY_MELDS;
                       }
               }
               return melds;
       }

       /**
        * Group や Sequence に利用でいない単騎のカードだけを抽出
        * @param cards 元になるカード集合
        * @return 単騎でしか利用できないカード集合
        */
       Cards singleCards(Cards cards) {
               // JOKER は対象外にする
               cards = cards.remove(Card.JOKER);
               Melds groupMelds = Melds.parseGroupMelds(cards);
               Melds sequenceMelds = Melds.parseSequenceMelds(cards);

               // Group 型の対象カードを削除
               for(Meld meld: groupMelds) {
                       cards = cards.remove(meld.asCards());
               }
               //エラー発見7/4
               // Sequence 型の対象カードを削除
               for(Meld meld: sequenceMelds) {
                       cards = cards.remove(meld.asCards());
               }

               return cards;
       }


       /**
        * 場が流れた直後の場に出すカードを決定
        * @param melds 手札で可能な役集合
        * @return 場に出すカード
        */
       Meld createRenewMeld() {
               Cards cards = this.hand();
               if(cards.size() > 2) {
                       cards.remove(Card.JOKER);
               }

               Melds melds = Melds.parseMelds(cards);

               if(this.place().order() == Order.NORMAL) {
                       melds = melds.extract(Melds.MIN_RANK);
               } else {
                       melds = melds.extract(Melds.MAX_RANK);
               }
               melds = melds.extract(Melds.MAX_SIZE);
               return checkJoker(melds.get(0));
       }

       /**
        * 最終候補が JOKER の単騎だった時の対処
        * @param meld 最終候補
        * @return JOKER の単騎の時には、最強のJOKERを返却
        */
       Meld checkJoker(Meld meld) {
               if(meld.type() == Meld.Type.SINGLE && meld.asCards().get(0) == Card.JOKER){
                       return createJokerMeld();
               } else {
                       return meld;
               }
       }

       Meld createJokerMeld() {
               //場のスートに合わせた,最大のランクを持つ役に変更して,それを出す．
               Suit suit;
               if(this.place().suits() == Suits.EMPTY_SUITS) {
                       // 取り合えず、スペードにしておく
                       suit = Suit.SPADES;
               } else {
                       suit = this.place().suits().get(0);
               }

               Rank rank;
               if(this.place().order() == Order.NORMAL) {
                       rank = Rank.JOKER_HIGHEST;
               } else {
                       rank = Rank.JOKER_LOWEST;
               }
               return MeldFactory.createSingleMeldJoker(suit, rank);
       }


       /**
        * 他のプレイヤが皆パスをしたかどうか
        * @return 皆パスをしていたら true
        */
       public boolean isOtherPlayerAllPassed() {
               for (int i = 0; i < 5; i++) {
                       if (number() == i || playerWon[i])
                               continue;
                       if (!playerPassed[i])
                               return false;
               }

               return true;
       }


       /**
        * 全員の残り手札の合計
        * @return 枚数
        */
       public int getNumberOfAllCards() {
               // プレイヤー全員の手札合計。減っていく。
               int[] allCards = playersInformation().numCardsOfPlayers();
               int total = 0;

               for (int cards : allCards) {
                       total += cards;
               }

               return total;
       }

       /**
        * 貧民戦かどうかを判定
        * @return 貧民戦ならtrue、そうでなければ false
        */
       public boolean isHinminsen() {
               int total = 0;
               for(int i = 0; i < 5; i++) {
                       if(number() == i || playerWon[i]) {
                               continue;
                       } else {
                               total++;
                       }
               }
               if(total > 1) {
                       return false;
               } else {
                       return true;
               }
       }

       public int numberOfStrongerCards(double rate) {
               boolean order = this.place().order() == Order.NORMAL;
               int rank = rankOfStrongerCards(rate);
               System.out.printf("stronger rank = %d%n", rank);

               Cards cards = this.hand();
               int count = 0;
               for(Card card: cards) {
                       if(card.equals(Card.JOKER)) {
                               count++;
                       } else if(order && card.rank().toInt() >= rank) {
                               count++;
                       } else if(!order && card.rank().toInt() <= rank) {
                               count++;
                       }
               }

               return count;
       }

       public int rankOfStrongerCards(double rate) {
               int total = 0;
               int stronger = 0;

               if (!playedCards[0][0]) {
                       total++;
                       stronger++;
               }
               for(int i = 0; i < 4; i++) {
                       for(int j = 1; j <=13; j++) {
                               if(!playedCards[i][j]) {
                                       total++;
                               }
                       }
               }

               int target = (int)((1.0 - rate) * total);
               boolean order = this.place().order() == Order.NORMAL;

               for(int j = (order? 15: 3); (order? j >= 3: j <= 15); j = j + (order? -1: 1)) {
                       int num = (j <= 13? j : j - 13);
                       for(int i = 0; i < 4; i++) {
                               if(!playedCards[i][num]) {
                                       stronger++;
                                       if(stronger > target) {
                                               return j;
                                       }
                               }
                       }
               }

               return (order? 3: 15);
       }

       /**
        * カードが現在の最強カードかどうかを判定
        * @param card 対象のカード
        * @param order 革命が起こっているかいないならば true
        * @return 最強の時に true
        */
       public boolean isStrongestCard(Card card, boolean order) {
               // 自分のカードより強いカードが出ているか判断する
               if (card == Card.JOKER) {
                       if(playedCards[Suit.SPADES.ordinal()][3] == true) {
                               // スペードの3が既に場に出ている
                               return true;
                       } else {
                               return false;
                       }
               } else if(card.rank() == Rank.EIGHT) {
                       return true;
               }

               if (playedCards[0][0] == false) {
                       // ジョーカーが未だ出ていなければ、最強ではない
                       return false;
               }

               int rankNum = card.rank().toInt();

               for (int i = rankNum + (order? 1: -1); (order? i <= 15: i >= 3); i = i + (order? 1: -1)) {
                       int num;

                       if (i >= 14) {
                               num = i - 13;
                       } else {
                               num = i;
                       }

                       // jはスートを調べる
                       for (int j = 0; j < 4; j++) {
                               if (!playedCards[j][num]) {
                                       return false;
                               }
                       }
               }
               return true;
       }

       /**
        * 役が現在の最強かどうかを判定
        * 現在は縛りは考慮していない
        * @param meld 対象の役
        * @param order 革命が起こっているかいないならば true
        * @return 最強の時に true
        */
       public boolean isStrongestMeld(Meld meld, boolean order) {
               if(meld.type() == Meld.Type.SINGLE) {
                       return isStrongestCard(meld.asCards().get(0), order);
               } else if(meld.type() == Meld.Type.GROUP) {
                       if(meld.rank() == Rank.EIGHT) {
                               return true;
                       }
                       int size = meld.asCards().size();

                       // JOKER が場に出ていない場合は、1枚少ない枚数でもGroupを組めるので、sizeを-1する
                       if(playedCards[0][0] == false) {
                               size--;
                       }

                       int rank = meld.rank().toInt();

                       for(int i = (order? rank + 1: rank - 1); (order? i <= 15: i >= 3); i = (order? i + 1: i - 1)) {
                               int rk = i;
                               if(rk > 13) rk -= 13;
                               int count = 0;
                               for(int j = 0; j < 4; j++) {
                                       if(playedCards[j][rk] == false) {
                                               count++;
                                       }
                               }
                               if(count >= size) {
                                       return false;
                               }
                       }
                       return true;
               } else if(meld.type() == Meld.Type.SEQUENCE) {
                       int length = meld.asCards().size();
                       int rank = meld.rank().toInt();
                       int count = 0;

                       if(rank <= 8 && 8 < rank + length ) {
                               return true;
                       }

                       for(int i = (order? rank + length: rank - length); (order? i <= 15 - length - 1: i >= 3 + length - 1); i = (order? i + 1: i - 1)) {
                               for(int j = 0; j < 4; j++) {
                                       if(playedCards[0][0] == false) {
                                               // Joker を相手が持っている場合は、1枚欠けていてもシーケンスが完成できる
                                               count = 1;
                                       }
                                       boolean found = true;
                                       for(int k = 0; k < length; k++) {
                                               int rk = (order? i + k: i - k);
                                               if(rk > 13) rk -= 13;
                                               if(playedCards[j][rk] == true) {
                                                       if(count == 0) {
                                                               found = false;
                                                               break;
                                                       } else {
                                                               count--;
                                                       }
                                               }
                                       }
                                       if(found) {
                                               return false;
                                       }
                               }
                       }
                       return true;
               } else {
                       return false;
               }
       }

       /**
        * 誰かが場に札を出す、あるいは、パスした時に呼び出されるメソッド
        */
       @Override
       public void played(java.lang.Integer number, Meld playedMeld) {
               super.played(number, playedMeld);
               try {

               // System.out.println(number + "さんが" + playedMeld.toString() + "を出しました。");

               if (playedMeld.asCards().isEmpty()) {
                       playerPassed[number] = true;
               }

               for (Card card : playedMeld.asCards()) {
                       // suit = マーク
                       if (card == Card.JOKER) {
                               playedCards[0][0] = true;
                       } else {
                               Suit suit = card.suit();
                               Rank rank = card.rank();
                               int suitNum = suit.ordinal();
                               int rankNum = rank.toInt();
                               if (rankNum > 13) {
                                       rankNum -= 13;
                               }
                               playedCards[suitNum][rankNum] = true;
                       }
               }
               } catch (Exception e) {
                       // 藤田: Exception が出たときの対処。何もしない
               }
       }

       /**
        * 場が流れた時に呼び出されるメソッド
        */
       @Override
       public void placeRenewed() {
               super.placeRenewed();

               try {
               // playerPassedの初期化
               for (int i = 0; i < 5; i++) {
                       playerPassed[i] = false;
               }
               } catch (Exception e) {
                       // 藤田: Exception が出た時の対処。何もしない
               }
       }

       /**
        * 新しいゲームがスタートする時に呼び出されるメソッド
        */
       @Override
       public void gameStarted() {
               super.gameStarted();
               try {
               // jokerは[0][0]
               playedCards = new boolean[4][14];
               playerPassed = new boolean[5];
               playerWon = new boolean[5];
               playedCardsCheck = false;
               } catch (Exception e) {
                       // 藤田: Exception が出た時の対処。何もしない
               }
       }

       /**
        * プレイヤが勝利したときに呼び出されるメソッド
        */
       @Override
       public void playerWon(java.lang.Integer number) {
               super.playerWon(number);
               try {
                       playerWon[number] = true;
               } catch (Exception e) {
                       // 藤田: Exception が出た時の対処。何もしない
               }
       }

       //カード交換
       public static Cards TradeJOKER(Cards cards){
               Cards removeHand = cards;
               //順番修正11_25
               // JokerとS3を両方持っているなら、S3は除外
               if (removeHand.contains(Card.JOKER, Card.S3)) {
                       removeHand = removeHand.remove(Card.S3);
               }
               // Jokerがあったら除外
               if (removeHand.contains(Card.JOKER)) {
                       removeHand = removeHand.remove(Card.JOKER);
               }
               return removeHand;
       }

       public static Cards removeTrade(Cards cards){
               Cards removeHand = cards;

               if (removeHand.contains(Card.D3)) {
                       removeHand = removeHand.remove(Card.D3);
               }
               //修正11/6 記述ミス
               if (removeHand.contains(Card.C8)) {
                       removeHand = removeHand.remove(Card.C8);
               }
               if (removeHand.contains(Card.H8)) {
                       removeHand = removeHand.remove(Card.H8);
               }
               if (removeHand.contains(Card.D8)) {
                       removeHand = removeHand.remove(Card.D8);
               }
               if (removeHand.contains(Card.S8)) {
                       removeHand = removeHand.remove(Card.S8);
               }
               return removeHand;
       }
       public void myCardsRemove(){
               int i;
               int j;
               Cards myHand = this.hand();
               for(Card card:myHand){
                       if(card == Card.JOKER){
                               playedCards[0][0] = true;
                       }else{
                               i = card.rank().toInt();
                               if(i >= 14){
                                       i = i - 13;
                               }
                               j = card.suit().ordinal();
                               playedCards[j][i] = true;
                       }
               }
               playedCardsCheck = true;
       }

       // 藤田: 却下のチェック
       @Override
       public void playRejected(java.lang.Integer number, Meld playedMeld) {
               super.playRejected(number, playedMeld);

               if(this.number().equals(number) && playedMeld != MeldFactory.PASS) {
//            	   if(this.place().type() == Meld.Type.SEQUENCE){
//            		   System.out.println("場のランク"+this.place().rank());
//            	   }
            	   //JOptionPane.showMessageDialog(null, "却下" + playedMeld);
               }
       }
}