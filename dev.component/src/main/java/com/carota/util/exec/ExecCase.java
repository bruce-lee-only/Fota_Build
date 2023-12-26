/*******************************************************************************
 * Copyright (C) 2018-2020 CAROTA Technology Crop. <www.carota.ai>.
 * All Rights Reserved.
 *
 * Unauthorized using, copying, distributing and modifying of this file,
 * via any medium is strictly prohibited.
 *
 * Proprietary and confidential.
 ******************************************************************************/

package com.carota.util.exec;

import com.carota.util.SerialExecutor;
import com.momock.util.Logger;

public class ExecCase {

    private SerialExecutor mExecutor = new SerialExecutor();

    private void launchCase(final String caseName) {
        mExecutor.setFinishNotify(new Runnable() {
            @Override
            public void run() {
                Logger.debug(caseName + " Finish");
            }
        });
        for(int i = 0; i < 10; i++) {
            final int id = i;
            mExecutor.execute(new Runnable() {
                @Override
                public void run() {
                    Logger.debug(caseName + " : Task start " + id);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Logger.debug(caseName + " : Task end " + id);
                }
            });
        }
    }

    public void launchCaseA() {
        launchCase("Case A");
    }

    public void launchCaseB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                launchCase("Case B");
                try {
                    Thread.sleep(2500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mExecutor.stop(null, new Runnable() {
                    @Override
                    public void run() {
                        Logger.debug("Case B STOP");
                    }
                });
            }
        }).start();

    }

    public void launchCaseC() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                launchCase("Case C");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mExecutor.stop(null, new Runnable() {
                    @Override
                    public void run() {
                        Logger.debug("Case C STOP");
                    }
                });
            }
        }).start();

    }

    public void launchCaseD() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                launchCase("Case D");
                mExecutor.stop(null, new Runnable() {
                    @Override
                    public void run() {
                        Logger.debug("Case D STOP");
                    }
                });
            }
        }).start();

    }

    public void launchCaseE() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                launchCase("Case E");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mExecutor.clearPending();
            }
        }).start();
    }
}
