package com.hy.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.hy.mylottery.R;

import android.content.Context;
import android.content.res.AssetManager;
import android.os.Environment;

public class ResourceUtils {

	private static String apikey = "";
	private static HashMap<String, String> initPushList;
	private static String mNeededPushListSync = "lock";

	private static String[] getLotteryNames() {
		return new String[] { "˫ɫ��", "����͸", "���ǲ�", "����3D", "����3", "����5", "14��ʤ����", "���ֲ�", "��3", "11ѡ5", "ʱʱ��", "��ѡ9��",
				"15ѡ5", "����6+1", "��������", "��������" };
	}

	private static Integer[] getLotteryIcons() {
		return new Integer[] { R.drawable.logo_ssq, R.drawable.logo_dlt, R.drawable.logo_qxc, R.drawable.logo_3d,
				R.drawable.logo_pl3, R.drawable.logo_pl5, R.drawable.logo_14csfc, R.drawable.logo_qlc,
				R.drawable.logo_kuai3, R.drawable.logo_11x5, R.drawable.logo_ssc, R.drawable.logo_rx9c,
				R.drawable.logo_15x5, R.drawable.logo_df6j1, R.drawable.logo_jclq, R.drawable.logo_jczq };
	}

	public static Map<Object, Object> getAllLotteries() {
		// keysĿǰû�о���ʵ�����壬�������ֱ�ʾ����������
		int key = 0;
		Object[] lotteryNames = getLotteryNames();
		Object[] lotteryIcons = getLotteryIcons();
		Map<Object, Object> data = new HashMap<Object, Object>();
		for (int i = 0; i < lotteryNames.length; i++) {
			Object[] lotteryResources = new Object[2];
			lotteryResources[0] = lotteryNames[i];
			lotteryResources[1] = lotteryIcons[i];
			data.put(++key, lotteryResources);
		}
		return data;
	}

	public static String[] getNavigationNames() {
		String[] navigationNames = new String[] { "���ʴ���", "���Ʒ���", "��������", "�ҵĲ�Ʊ" };
		return navigationNames;
	}

	public static int[] getUnpressedNavigationIcons() {
		int[] unpressedNavigationIcons = new int[] { R.drawable.tab_hall_unpressed, R.drawable.tab_trend_unpressed,
				R.drawable.tab_reward_unpressed, R.drawable.tab_user_unpressed };
		return unpressedNavigationIcons;
	}

	public static int[] getPressedNavigationIcons() {
		int[] pressedNavigationIcons = new int[] { R.drawable.tab_hall_pressed, R.drawable.tab_trend_pressed,
				R.drawable.tab_reward_pressed, R.drawable.tab_user_pressed };
		return pressedNavigationIcons;
	}

	// ������������APIKEY, Free api link: http://apistore.baidu.com/apiworks/servicedetail/164.html
	public static String getApikey(Context context) {
		if (apikey.equals("")) {
			if (context == null) {
				return "error: context == null";
			}
			AssetManager am = context.getAssets();
			BufferedReader reader = null;
			try {
				reader = new BufferedReader(new InputStreamReader(am.open("apikey.bin")));
				String line;
				if ((line = reader.readLine()) != null) {
					apikey += line;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (reader != null) {
					try {
						reader.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

		}

		return apikey;
	}

	// ��ȡ���п����Ϳ�����Ϣ��Ʊ��
	public static HashMap<String, String> getInitPushList(Context context) {
		if (initPushList == null) {
			initPushList = new HashMap<String, String>();
			AssetManager am = context.getAssets();
			InputStream fis = null;
			BufferedReader reader = null;
			try {
				fis = am.open("initpushlist/pushList.bin");
				reader = new BufferedReader(new InputStreamReader(fis, Charset.forName("GBK")));
				String line = null;
				while ((line = reader.readLine()) != null) {
					String[] keyValue = line.split(",");
					initPushList.put(keyValue[0], keyValue[1]);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
					if (reader != null) {
						reader.close();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
				}
			}
		}
		return initPushList;
	}

	// ��ȡ��Ҫ���Ϳ�����Ϣ��Ʊ��
	@SuppressWarnings("unchecked")
	public static HashMap<String, String> getNeededPushList(Context context) {
		synchronized (mNeededPushListSync) {
			HashMap<String, String> neededPushList = null;
			// loading exist needed notice lottery list
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String externalPath = Environment.getExternalStorageDirectory().getPath();
				File file = new File(externalPath, "oneLottery/pushlist/alarmPush.bin");
				ObjectInputStream ois = null;
				try {
					ois = new ObjectInputStream(new FileInputStream(file));
					neededPushList = (HashMap<String, String>) ois.readObject();
				} catch (StreamCorruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (ois != null) {
						try {
							ois.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

			// if no exist list, load the initial data
			if (neededPushList == null) {
				neededPushList = (HashMap<String, String>) ResourceUtils.getInitPushList(context).clone();
				// crash if don't get the clone,
			}

			return neededPushList;
		}
	}

	// ������Ҫ���Ϳ�����Ϣ��Ʊ��
	public static void putNeededPushList(HashMap<String, String> list) {
		synchronized (mNeededPushListSync) {
			if (list != null && Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String externalPath = Environment.getExternalStorageDirectory().getPath();
				File dir = new File(externalPath, "oneLottery/pushlist");
				ObjectOutputStream oos = null;
				try {
					if (!dir.exists()) {
						dir.mkdir();
					}
					File file = new File(dir, "alarmPush.bin");
					oos = new ObjectOutputStream(new FileOutputStream(file));
					oos.writeObject(list);
				} catch (StreamCorruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (oos != null) {
						try {
							oos.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
						}
					}
				}
			}
		}
	}
}
