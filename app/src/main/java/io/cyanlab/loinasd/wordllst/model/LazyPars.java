package io.cyanlab.loinasd.wordllst.model;
import android.os.Environment;
import android.widget.Toast;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;


import io.cyanlab.loinasd.wordllst.R;

/**
 * Created by Анатолий on 08.09.2017.
 */

class LazyPars {

    public Wordlist createWl (){
        try {
           Scanner sc;
           sc = new Scanner("responsible/reliable/trustworthy \tОтвественный\n" +
                   "bossy/dictatorial \tЛюбящий командовать\n" +
                   "energetic/active/lively \tАктивный\n" +
                   "self-centered/selfish \tСамовлюбленный\n" +
                   "determined/decided/decisive/confident \tРешительный\n" +
                   "reserved/blank \tПустой\n" +
                   "secretive \tСкрытный\n" +
                   "emotional/passionate/hot-blooded \tЭмоциональный\n" +
                   "immediate/direct \tПрямой\n" +
                   "mean/thrifty/economical/materialistic \tЖадный\n" +
                   "generous \tЩедрый\n" +
                   "possessive/jealous/envious/covetous \tЗавистливый\n" +
                   "sensitive/impressionable \tЧувствительный\n" +
                   "cold-blooded/cool-hearted/dispassionate \tБесчувственный\n" +
                   "intelligent/genius/wise/smart/quick-witted \tУмный\n" +
                   "arrogant/proud/conceited/vain \tГордый\n" +
                   "haughty/hostile/unfriendly \tВраждебный\n" +
                   "cooperative/obedient/law-abiding \tПослушный\n" +
                   "shy/timid \tЗастенчивый\n" +
                   "humble/modest \tСкромный\n" +
                   "absent-minded/forgetful \tЗабывчивый\n" +
                   "loyal/faithful \tПреданный\n" +
                   "aggressive/rude/impolite \tАгрессивный\n" +
                   "authoritarian/strict/severe/stern \tСтрогий\n" +
                   "boastful \tХвастливый\n" +
                   "compassionate/sympathetic/supportive \tСострадательный\n" +
                   "competitive/ambitious \tАмбициозный\n" +
                   "confident/self-confident/self-assured \tУверенный в себе\n" +
                   "curious/inquisitive \tЛюбопытный\n" +
                   "disciplined \tДисциплинированный\n" +
                   "easy-going/relaxed/light-hearted/carefree/careless \tБеззаботный\n" +
                   "careful/diligent/petty/meticulous/thorough/cautious \tАккуратный\n" +
                   "hardworking/industrious/efficient/productive \tТрудолюбивый\n" +
                   "lazy \tЛенивый\n" +
                   "enthusiastic/keen/eager \tЭнтузиаст\n" +
                   "gentle/nice/empathetic/considerate \tТактичный\n" +
                   "shallow/impulsive \tПоверхностный\n" +
                   "stable/well-balanced \tУравновешенный\n" +
                   "creative/resourceful/inventive \tКреативный\n" +
                   "immature \tИнфантильный\n" +
                   "indecisive \tНерешительный\n" +
                   "innocent/sincere/honest \tЧестный\n" +
                   "insincere/two-faced/dishonest \tБесчестный\n" +
                   "intellectual/sensible/reasonable/logical \tЛогичный\n" +
                   "intolerant/stubborn \tУпертый\n" +
                   "moody \tУнылый\n" +
                   "narrow-minded/conservative \tКонсервативный\n" +
                   "open-minded/liberal \tЛиберальный\n" +
                   "outgoing/communicative/sociable \tОбщительный\n" +
                   "patient \tТерпеливый\n" +
                   "pessimistic/resigned \tПессимистичный\n" +
                   "rebellious/disobedient \tНепослушный\n" +
                   "strong-willed \tВолевой\n" +
                   "tactless  \tБестактный\n" +
                   "unpredictable \tНепредсказуемый\n" +
                   "unprincipled \tБеспринципный\n" +
                   "cheerful/life-affirming \tБеззаботный\n" +
                   "content(ed)/happy/satisfied \tСчастливый\n" +
                   "insecure/screwed up/having low self-esteem \tНеуверенный в себе\n");
           ArrayList<Line> lines = new ArrayList<Line>();
           while (sc.hasNextLine()) {
               Line line = new Line();
               String s = sc.nextLine();
               int ind = s.indexOf("/");
               int prevInd = 0;
               while ( (ind != s.lastIndexOf("/")) && (s.charAt(ind) == '/') &&(ind != -1)) {
                   Word word = new Word(s.substring(prevInd, ind - 1), Lang.EN, line);
                   prevInd = ind;
                   s = s.replace('/','1');
                   ind = s.indexOf("/");
                   line.getPrime().add(word);
               }
               s = s.substring(s.indexOf("     ")+2);
               Word word = new Word(s, Lang.RU, line);
               line.getTranslate().add(word);
               lines.add(line);
           }
           return new Wordlist("Kek", lines);
        } catch (Exception e){
            return new Wordlist("blet",null);
        }



    }


}
