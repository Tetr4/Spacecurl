
package de.klimek.spacecurl.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.List;

import de.klimek.spacecurl.Database;
import de.klimek.spacecurl.R;
import de.klimek.spacecurl.game.GameDescription;
import de.klimek.spacecurl.util.cards.GameCard;
import de.klimek.spacecurl.util.collection.Training;
import it.gmariotti.cardslib.library.internal.Card;
import it.gmariotti.cardslib.library.internal.CardArrayAdapter;
import it.gmariotti.cardslib.library.view.CardListView;

/**
 * Launched when choosing to edit a training or create a new one. If a
 * training's database index is given as an extra (EXTRA_TRAINING_INDEX), the
 * training will be edited. Otherwise a new training will be created.
 * 
 * @author Mike Klimek
 */
public class TrainingBuilderActivity extends FragmentActivity {
    public static final String TAG = TrainingBuilderActivity.class.getName();
    public static final String EXTRA_TRAINING_INDEX = "EXTRA_TRAINING_INDEX";
    private Database mDatabase = Database.getInstance(this);
    private Training mTraining;

    private CardListView mCardListView;
    private CardArrayAdapter mCardArrayAdapter;
    private List<Card> mCards = new ArrayList<Card>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_builder);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        Bundle extras = getIntent().getExtras();
        if (getIntent().getExtras() != null) {
            // edit existing training
            int key = extras.getInt(EXTRA_TRAINING_INDEX);
            mTraining = mDatabase.getTrainings().get(key);
        } else {
            // create new Training
            mTraining = new Training("New Training"); // TODO Resource String
            mDatabase.getTrainings().add(mTraining);
            askForTitle();
        }

        setupCards();
        setupAddGameButton();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mDatabase.isOrientationLandscape()) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void setupAddGameButton() {
        ImageButton btn = (ImageButton) findViewById(R.id.game_add_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askForGame();
            }
        });
    }

    /**
     * show dialog, which lets the user select a game to add
     */
    private void askForGame() {
        ContextThemeWrapper contextWithThemeHolo = new ContextThemeWrapper(this,
                android.R.style.Theme_Holo_Dialog);

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(contextWithThemeHolo);
        alertBuilder.setTitle(getResources().getString(R.string.training_addbutton));

        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                contextWithThemeHolo, android.R.layout.select_dialog_singlechoice);
        for (GameDescription curGame : mDatabase.getSelectableGames()) {
            arrayAdapter.add(curGame.getTitle());
        }

        alertBuilder.setAdapter(arrayAdapter,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        addGame(mDatabase.getSelectableGames().get(which));
                    }

                });
        alertBuilder.show();
    }

    /**
     * show dialog which will ask for the training's title
     */
    private void askForTitle() {
        ContextThemeWrapper contextWithThemeHolo = new ContextThemeWrapper(this,
                android.R.style.Theme_Holo_Dialog);
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(contextWithThemeHolo);

        alertBuilder.setTitle(getResources().getString(R.string.training_name_hint));
        final EditText input = new EditText(contextWithThemeHolo);
        // TODO default input
        alertBuilder.setView(input);
        // TODO resource String
        alertBuilder.setPositiveButton("Ok",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String title = input.getText().toString();
                        if (!title.isEmpty()) {
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
                // TODO show undo notifcation
                finish();
                break;
            case R.id.action_play:
                int key = mDatabase.getTrainings().indexOf(mTraining);
                Intent intent = new Intent(this, TrainingActivity.class);
                intent.putExtra(TrainingActivity.EXTRA_TRAINING_INDEX, key);
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
        for (GameDescription curGame : mTraining) {
            mCards.add(new GameCard(this, curGame));
        }
        mCardArrayAdapter = new CardArrayAdapter(this, mCards);
        mCardListView = (CardListView) findViewById(R.id.game_list);
        mCardListView.setAdapter(mCardArrayAdapter);
        View footerItem = getLayoutInflater()
                .inflate(R.layout.list_item_training_footer, mCardListView, false);
        mCardListView.addFooterView(footerItem);
        mCardArrayAdapter.notifyDataSetChanged();
    }

    public void addGame(GameDescription game) {
        mTraining.add(game);
        mCards.add(new GameCard(this, game));
        mCardArrayAdapter.notifyDataSetChanged();
    }

    public void removeGame(GameCard card) {
        mTraining.remove(card.getGameDescription());
        mCards.remove(card);
        mCardArrayAdapter.notifyDataSetChanged();
    }

}
