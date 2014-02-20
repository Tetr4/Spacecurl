
package de.klimek.spacecurl.training;

import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.TrainingActivity;
import de.klimek.spacecurl.game.GameSettings;
import de.klimek.spacecurl.util.collection.Database;
import de.klimek.spacecurl.util.collection.training.Training;

public class TrainingBuilderActivity extends FragmentActivity {
    public static final int NEW_TRAINING = Integer.MIN_VALUE;
    private Database mDatabase = Database.getInstance(this);
    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();
    private Training mTraining;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        setContentView(R.layout.activity_training_builder);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        int key = getIntent().getExtras().getInt(TrainingActivity.EXTRA_TRAINING_KEY);
        if (key != NEW_TRAINING) {
            mTraining = mDatabase.getTrainings().get(key);
            setupCards();
        } else { // new Training
            mTraining = new Training("New Training"); // TODO Resource
            mDatabase.getTrainings().add(mTraining);
            askForTitle();
            setupCards();
        }
        setupAddButton();
    }

    private void setupAddButton() {
        ImageButton btn = (ImageButton) findViewById(R.id.game_add_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForGame();
            }
        });
    }

    private void askForGame() {
        ContextThemeWrapper contextWithThemeHolo = new ContextThemeWrapper(this,
                android.R.style.Theme_Holo_Dialog);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(contextWithThemeHolo);
        alertBuilder.setTitle("List");

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                contextWithThemeHolo,
                android.R.layout.select_dialog_singlechoice);
        for (GameSettings curGame : mDatabase.getTrainingGames()) {
            arrayAdapter.add(curGame.getTitle());
        }

        alertBuilder.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addGame(mDatabase.getTrainingGames().get(which));
                    }

                });
        alertBuilder.show();
    }

    private void askForTitle() {
        // Dialog which will ask for the title
        ContextThemeWrapper contextWithThemeHolo = new ContextThemeWrapper(this,
                android.R.style.Theme_Holo_Dialog);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(contextWithThemeHolo);
        // TODO resource ID
        alertBuilder.setTitle("Enter an name for the training");
        final EditText input = new EditText(contextWithThemeHolo);
        // TODO default input
        alertBuilder.setView(input);
        // TODO resource ID
        alertBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = input.getText().toString();
                        if (!"".equals(title)) {
                            mTraining.setTitle(title);
                        }
                    }
                });
        alertBuilder.create().show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.training_builder, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                mDatabase.getTrainings().remove(mTraining);
                finish();
                // TODO Remove from database
                // TODO go back
                // TODO show undo notifcation
                break;
            case R.id.action_play:
                int key = mDatabase.getTrainings().indexOf(mTraining);
                Intent intent = new Intent(this, TrainingActivity.class);
                intent.putExtra(TrainingActivity.EXTRA_TRAINING_KEY, key);
                startActivity(intent);
                break;
            case R.id.action_rename:
                askForTitle();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void setupCards() {
        for (GameSettings curGame : mTraining) {
            mCards.add(createGameCard(curGame));
        }
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.game_list);
        mCardListView.setAdapter(mCardArrayAdapter);
    }

    public void addGame(GameSettings settings) {
        mTraining.add(settings);
        mCards.add(createGameCard(settings));
        mCardArrayAdapter.notifyDataSetChanged();
    }

    public void removeGame(GameCard card) {
        mTraining.remove(card.getGameSettings());
        mCards.remove(card);
        mCardArrayAdapter.notifyDataSetChanged();
    }

    private GameCard createGameCard(GameSettings settings) {
        GameCard card = new GameCard(this, settings);
        return card;
    }

    private static class GameCard extends Card {
        private TextView mGameTitleTextView;
        private String mGameTitle;
        private GameSettings mGameSettings;

        public GameCard(Context context, GameSettings gameSettings) {
            super(context, R.layout.card_game);
            setGameSettings(gameSettings);
            mGameTitle = gameSettings.getTitle();
        }

        @Override
        public void setupInnerViewElements(ViewGroup parent, View view) {
            mGameTitleTextView = (TextView) parent.findViewById(
                    R.id.card_game_name);
            mGameTitleTextView.setText(mGameTitle);
        }

        public GameSettings getGameSettings() {
            return mGameSettings;
        }

        public void setGameSettings(GameSettings gameSettings) {
            mGameSettings = gameSettings;
        }

    }

}
