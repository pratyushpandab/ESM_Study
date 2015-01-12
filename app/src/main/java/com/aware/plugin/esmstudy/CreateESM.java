package com.aware.plugin.esmstudy;


import com.aware.ESM;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import java.util.Random;


// create ESM questions
public class CreateESM extends IntentService {

    //public static final int SET_SIZE_REQUIRED = 3; // to generate 3 random numbers from 0 - 9
    public static int counter;
    public static String CONDITION = null;
    public static int randomInt;
    public static final int NUMBER_RANGE = 10;
    public static final int QPQ = 5; // QUESTIONS_PER_QUESTIONNAIRE
    public static String[] ESMqueue = new String[5]; // string to hold the ESM queue
    public static final String[] ESMquestion = new String[5]; // string to hold the ESM Questionnaire
    public static String esm_trigger;
    public static int requestCode;

    // QType1 is the Likert scale questions, i.e. the personality trait pairs
    public static final String[] option = new String[10]; // string to hold the personality trait pairs
    public static final String QType1_start = "{'esm':{" +
            "'esm_type':" + ESM.TYPE_ESM_LIKERT + "," +
            "'esm_title': '";

    public static final String QType1_Instruction = "'," + "'esm_instructions': 'Above pair of traits apply to you in GENERAL.',";
    public static final String QType1_end = "'esm_likert_max': 5," +
            "'esm_likert_max_label': 'Agree Strongly'," +
            "'esm_likert_min_label': 'Disagree Strongly'," +
            "'esm_likert_step': 1," +
            "'esm_submit': 'Next'," +
            "'esm_expiration_threashold': 1200,";


    // QType2 is the Open ended questions
    public static final String[] QType2_Instruction = new String[2]; // string to hold the open ended questions
    public static final String QType2_start1 = "{'esm':{" +
            "'esm_type':" + ESM.TYPE_ESM_TEXT + "," +
            "'esm_title': 'Since the last survey, WHAT have you felt and WHY?'," +
            "'esm_instructions': 'Please answer as elaborately as possible.',";


    public static final String QType2_start2 = "{'esm':{" +
            "'esm_type':" + ESM.TYPE_ESM_TEXT + "," +
            "'esm_title': 'Explain the MAIN ACTIVITY you are doing.'," +
            "'esm_instructions': 'Please answer as elaborately as possible',";

    public static final String QType2_end = "'esm_submit': 'Next'," +
            "'esm_expiration_threashold': 1200,";



    // QType3 is the Check Box question
    public static final String QType3 = "{'esm':{" +
            "'esm_type':" + ESM.TYPE_ESM_CHECKBOX + "," +
            "'esm_title': 'Who is with you, NOW.'," +
            "'esm_instructions': 'You can choose multiple options.'," +
            "'esm_checkboxes':['Alone', 'Friend(s)', 'Family member(s)', 'Stranger(s)', 'Other']," +
            "'esm_submit': 'Next'," + //submit button label
            "'esm_expiration_threashold': 1200,"; //the user has 60 seconds to respond. Set to 0 to disable


    // QType4 is Likert scale question
    public static final String QType4 = "{'esm':{" +
            "'esm_type':" + ESM.TYPE_ESM_LIKERT + "," +
            "'esm_title':        'CURRENT valence'," +
            "'esm_instructions': 'e.g. Negative: Sad, Angry, Irritable, Lonely.\n" +
            "e.g. Positive: Happy, Friendly, Cheerful, Sociable.'," +
            "'esm_likert_max': 5," +
            "'esm_likert_max_label': 'Positive'," +
            "'esm_likert_min_label': 'Negative'," +
            "'esm_likert_step': 1," +
            "'esm_submit': 'Next'," + //submit button label
            "'esm_expiration_threashold': 1200,"; //the user has 60 seconds to respond. Set to 0 to disable


    // QType5 is Likert scale question
    public static final String QType5 = "{'esm':{" +
            "'esm_type':" + ESM.TYPE_ESM_LIKERT + "," +
            "'esm_title':        'CURRENT arousal'," +
            "'esm_instructions': 'e.g. Passive: Drowsy, Weak, Detached, Bored.\n" +
            "e.g. Active : Alert, Strong, Involved, Excited.'," +
            "'esm_likert_max': 5," +
            "'esm_likert_max_label': 'Active'," +
            "'esm_likert_min_label': 'Passive'," +
            "'esm_likert_step': 1," +
            "'esm_submit': 'Next'," + //submit button label
            "'esm_expiration_threashold': 1200,"; //the user has 60 seconds to respond. Set to 0 to disable


    public CreateESM(){
        super("CreateESM");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        counter = intent.getIntExtra("counter", 0);
        requestCode = intent.getIntExtra("requestCode", 0);
        CONDITION = intent.getStringExtra("CONDITION_" + requestCode);
        System.out.println("received from Plugin :: COUNTER :: " + counter);
        populateOptions(); // populate the values for the Likert scale and Open ended questions
        randomGenerator(); // generate 3 unique random numbers
        makeQuestion(); // generate the ESM queue
        shuffleESMqueue(); // shuffle the order of the questions in the ESMqueue
        displayESM(getApplicationContext(), ESMqueue[counter]); // Display ESM Questions on the phone
    }

    // populate the values for the Likert scale and Open ended questions
    public void populateOptions()
    {
        option[0] = "Extraverted, Enthusiastic";
        option[1] = "Critical, Quarrelsome";
        option[2] = "Dependable, Self-disciplined";
        option[3] = "Anxious, Easily upset";
        option[4] = "Open to new experiences, Complex";
        option[5] = "Reserved, Quiet";
        option[6] = "Sympathetic, Warm";
        option[7] = "Disorganized, Careless";
        option[8] = "Calm, Emotionally stable";
        option[9] = "Conventional, Uncreative";

        QType2_Instruction[0] = QType2_start1;
        QType2_Instruction[1] = QType2_start2;

        esm_trigger = "'esm_trigger': '" + CONDITION + "'" +
                "}}";

        System.out.println("populate options");
    }

    // generate 1 random number
    @SuppressLint("Assert")
    public void randomGenerator() {
        Random randomGenerator = new Random();
        randomInt = randomGenerator.nextInt(NUMBER_RANGE);
    }

    // generate the ESM queue
    public void makeQuestion() {

        ESMquestion[0] = QType1_start + option[randomInt] + QType1_Instruction + QType1_end + esm_trigger;

        int random_number = randomInt % 2;
        ESMquestion[1] = QType2_Instruction[random_number] + QType2_end + esm_trigger;
        ESMquestion[2] = QType3 + esm_trigger;
        ESMquestion[3] = QType4 + esm_trigger;
        ESMquestion[4] = QType5 + esm_trigger;
        for (int i = 0; i < QPQ; i++) {
            ESMqueue[i] = "[" + ESMquestion[i] + "]";
        }
        System.out.println("ESM queue created");
    }

    // shuffle the order of the questions in the ESMqueue
    public void shuffleESMqueue() {
        Random random = new Random();
        for (int i = QPQ - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            // swap
            String s = ESMqueue[index];
            ESMqueue[index] = ESMqueue[i];
            ESMqueue[i] = s;
        }
        System.out.println("ESM queue shuffled");
    }

    // Display ESM Questions on the phone
    public static void displayESM(Context context, String esm) {
        Intent queue_esm = new Intent(ESM.ACTION_AWARE_QUEUE_ESM);
        queue_esm.putExtra(ESM.EXTRA_ESM, esm);
        context.sendBroadcast(queue_esm);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
