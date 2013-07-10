package com.mikhaellopez.gmailapi;

import java.io.IOException;
import java.util.Arrays;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "API_GMAIL";
	private TextView textViewName;
	private TextView textViewNumConversations;
	private TextView textViewNumUnreadConversation;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.activity_main);
		
		textViewName = (TextView)findViewById(R.id.textViewName);
		textViewNumConversations = (TextView)findViewById(R.id.textViewNumConversations);
		textViewNumUnreadConversation = (TextView)findViewById(R.id.textViewNumUnreadConversation);
	}

	@Override
	public void onResume() {
		super.onResume();
		checkNoReadMessageGmailActionBar();
	}

	private void onAccountResults(Account[] accounts) {
		Log.i(TAG, "received accounts: " + Arrays.toString(accounts));
		if (accounts != null && accounts.length > 0) {
			// Pick the first one, and display a list of labels
			final String account = accounts[0].name;
			Log.i(TAG, "Starting loader for labels of account: " + account);
			final Bundle args = new Bundle();
			args.putString("account", account);
			getLoaderManager().restartLoader(0, args, new LoaderCallbacks<Cursor>() {
				@Override
				public Loader<Cursor> onCreateLoader(int id, Bundle args) {
					final String account = args.getString("account");
					final Uri labelsUri = GmailContract.Labels.getLabelsUri(account);
					return new CursorLoader(MainActivity.this, labelsUri, null, null, null, null);
				}

				@Override
				public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
					if (cursor != null) {
						String nameCol = cursor.getColumnName(3);
						String numConversationsCol = cursor.getColumnName(4);
						String numUnreadConversationsCol = cursor.getColumnName(5);
						
						if(cursor.moveToFirst()) {
							String nameVal = cursor.getString(3);
							int numConversationsVal = cursor.getInt(4);
							int numUnreadConversationVal = cursor.getInt(5);
							
							textViewName.setText(nameCol + " : " + nameVal);
							textViewNumConversations.setText(numConversationsCol + " : " + numConversationsVal);
							textViewNumUnreadConversation.setText(numUnreadConversationsCol + " : " + numUnreadConversationVal);
						}
					}
				}
				@Override
				public void onLoaderReset(Loader<Cursor> arg0) {}
			});
		}
	}

	private void checkNoReadMessageGmailActionBar() {
		final String ACCOUNT_TYPE_GOOGLE = "com.google";
		final String[] FEATURES_MAIL = {"service_mail"};

		AccountManager.get(this).getAccountsByTypeAndFeatures(ACCOUNT_TYPE_GOOGLE, FEATURES_MAIL,
				new AccountManagerCallback<Account[]>() {
					@Override
					public void run(AccountManagerFuture<Account[]> future) {
						Account[] accounts = null;
						try {
							accounts = future.getResult();
						} catch (OperationCanceledException oce) {
							Log.e(TAG, "Got OperationCanceledException", oce);
						} catch (IOException ioe) {
							Log.e(TAG, "Got OperationCanceledException", ioe);
						} catch (AuthenticatorException ae) {
							Log.e(TAG, "Got OperationCanceledException", ae);
						}
						onAccountResults(accounts);
					}
				}, null);
	}

}
