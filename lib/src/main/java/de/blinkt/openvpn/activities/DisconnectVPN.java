/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.View;

import de.blinkt.openvpn.LaunchVPN;
import dora.lifecycle.walletconnect.R;
import de.blinkt.openvpn.core.IOpenVPNServiceInternal;
import de.blinkt.openvpn.core.OpenVPNService;
import de.blinkt.openvpn.core.ProfileManager;
import de.blinkt.openvpn.core.VpnStatus;
import dora.trade.DoraTrade;
import dora.widget.DoraAlertDialog;

/**
 * Created by arne on 13.10.13.
 */
public class DisconnectVPN extends Activity implements DialogInterface.OnCancelListener {
    private IOpenVPNServiceInternal mService;
    private ServiceConnection mConnection = new ServiceConnection() {



        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {

            mService = IOpenVPNServiceInternal.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mService = null;
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, OpenVPNService.class);
        intent.setAction(OpenVPNService.START_SERVICE);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        showDisconnectDialog();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(mConnection);
    }

    private void showDisconnectDialog() {
        DoraAlertDialog dialog = new DoraAlertDialog(this);
        dialog.title(getString(R.string.title_cancel));
        dialog.message(getString(R.string.cancel_connection_query));
        dialog.themeColor(DoraTrade.INSTANCE.getThemeColor());
        dialog.positiveButton(getString(R.string.cancel_connection));
        dialog.negativeButton(getString(R.string.reconnect));
        dialog.positiveListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileManager.setConntectedVpnProfileDisconnected(DisconnectVPN.this);
                if (mService != null) {
                    try {
                        mService.stopVPN(false);
                    } catch (RemoteException e) {
                        VpnStatus.logException(e);
                    }
                }
                finish();
            }
        });
        dialog.negativeListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DisconnectVPN.this, LaunchVPN.class);
                intent.putExtra(LaunchVPN.EXTRA_KEY, VpnStatus.getLastConnectedVPNProfile());
                intent.setAction(Intent.ACTION_MAIN);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        finish();
    }
}
