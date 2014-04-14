package com.leonlee.windplayer.util;

import io.vov.vitamio.ThumbnailUtils;
import io.vov.vitamio.provider.MediaStore.Video;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import com.leonlee.windplayer.po.PFile;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class FileUtils {
    private static String TAG = "FileUtils";
    
	// http://www.fileinfo.com/filetypes/video
	public static final String[] VIDEO_EXTENSIONS = {
		"264", "3g2", "3gp", "3gp2", "3gpp", "3gpp2", /*"3mm", "3p2", */
		/*"60d", "aep", "ajp", "amv", "amx", "arf", "asf", "asx", "avb",*/
		/*"avd",*/ "avi", /*"avs", "avs", "axm", "bdm", "bdmv", "bik", "bin",
		"bix", "bmk", "box", "bs4", "bsf", "byu", "camre", "clpi", "cpi",
		"cvc", "d2v", "d3v", "dat", "dav", "dce", "dck", "ddat", "dif",
		"dir",*/ "divx", /*"dlx", "dmb", "dmsm", "dmss", "dnc", "dpg",
		"dream", "dsy", "dv", "dv-avi", "dv4", "dvdmedia", "dvr-ms",
		"dvx", "dxr", "dzm", "dzp", "dzt", "evo", "eye", "f4p",*/ "f4v",
		/*"fbr", "fbr", "fbz", "fcp", "flc", "flh", "fli",*/ "flv", /*"flx",
		"gl", "grasp", "gts", "gvi", "gvp", "hdmov", "hkm", "ifo", "imovi",
		"imovi", "iva", "ivf", "ivr", "ivs", "izz", "izzy", "jts", "lsf",
		"lsx", "m15", "m1pg", "m1v", "m21", "m21", "m2a", "m2p", "m2t",*/ "m2ts",
		/*"m2v", "m4e", "m4u",*/ "m4v", /*"m75", "meta", "mgv", "mj2", "mjp", "mjpg",*/
		"mkv", /*"mmv", "mnv", "mod", "modd", "moff", "moi", "moov",*/ "mov", /*"movie",
		"mp21", "mp21", "mp2v",*/ "mp4", /*"mp4v", "mpe",*/ "mpeg", "mpeg4", "mpf", "mpg",
		/*"mpg2", "mpgin", "mpl", "mpls", "mpv", "mpv2", "mqv", "msdvd", "msh", "mswmm",
		"mts", "mtv", "mvb", "mvc", "mvd", "mve", "mvp", "mxf", "mys", "ncor", "nsv",
		"nvc", "ogm", "ogv", "ogx", "osp", "par", "pds", "pgi", "piv", "playlist",
		"pmf", "prel", "pro", "prproj", "psh", "pva", "pvr", "pxv", "qt", "qtch",
		"qtl", "qtm", "qtz", "rcproject", "rdb", "rec",*/ "rm", /*"rmd", "rmp", "rms",*/
		"rmvb", /*"roq", "rp", "rts", "rts", "rum", "rv", "sbk", "sbt", "scm", "scm",
		"scn", "sec", "seq", "sfvidcap", "smil", "smk", "sml", "smv", "spl", "ssm",
		"str", "stx", "svi",*/ "swf", /*"swi", "swt", "tda3mt", "tivo", "tix", "tod", "tp",
		"tp0", "tpd", "tpr", "trp",*/ "ts", /*"tvs", "vc1", "vcr", "vcv", "vdo", "vdr",
		"veg", "vem", "vf", "vfw", "vfz", "vgz", "vid", "viewlet", "viv", "vivo",
		"vlab", "vob", "vp3", "vp6", "vp7", "vpj", "vro", "vsp", "w32", "wcp",*/ "webm",
		/*"wm", "wmd", "wmmp",*/ "wmv", /*"wmx", "wp3", "wpl", "wtv", "wvx", "xfl",*/ "xvid",
		"yuv", /*"zm1", "zm2", "zm3", "zmv"*/ };
		
	// http://www.fileinfo.com/filetypes/audio
	public static final String[] AUDIO_EXTENSIONS = {
		/*"4mp", "669", "6cm", "8cm", "8med", "8svx", "a2m", "aa", "aa3",*/ "aac", /*"aax",
		"abc", "abm",*/ "ac3", /*"acd", "acd-bak", "acd-zip", "acm", "act", "adg", "afc",
		"agm", "ahx", "aif", "aifc", "aiff", "ais", "akp", "al", "alaw", "all", "amf",*/
		"amr", /*"ams", "ams", "aob",*/ "ape", /*"apf", "apl", "ase", "at3", "atrac", "au",
		"aud", "aup", "avr", "awb", "band", "bap", "bdd", "box", "bun", "bwf", "c01",
		"caf", "cda", "cdda", "cdr", "cel", "cfa", "cidb", "cmf", "copy", "cpr", "cpt",
		"csh", "cwp", "d00", "d01", "dcf", "dcm", "dct", "ddt", "dewf", "df2", "dfc",
		"dig", "dig", "dls", "dm", "dmf", "dmsa", "dmse", "drg", "dsf", "dsm", "dsp",
		"dss", "dtm",*/ "dts", /*"dtshd", "dvf", "dwd", "ear", "efa", "efe", "efk", "efq",
		"efs", "efv", "emd", "emp", "emx", "esps", "f2r", "f32", "f3r", "f4a", "f64",
		"far", "fff",*/ "flac", /*"flp", "fls", "frg", "fsm", "fzb", "fzf", "fzv", "g721",
		"g723", "g726", "gig", "gp5", "gpk", "gsm", "gsm", "h0", "hdp", "hma", "hsb",
		"ics", "iff", "imf", "imp", "ins", "ins", "it", "iti", "its", "jam", "k25",
		"k26", "kar", "kin", "kit", "kmp", "koz", "koz", "kpl", "krz", "ksc", "ksf",
		"kt2", "kt3", "ktp", "l", "la", "lqt", "lso", "lvp", "lwv", "m1a",*/ "m3u", "m4a",
		"m4b", "m4p", "m4r", /*"ma1", "mdl", "med", "mgv",*/ "mid", "midi", /*"miniusf", "mka",
		"mlp", "mmf", "mmm", "mmp", "mo3", "mod",*/ "mp1", "mp2", "mp3", "mpa", "mpc", "mpga",
		/*"mpu", "mp_", "mscx", "mscz", "msv", "mt2", "mt9", "mte", "mti", "mtm", "mtp", "mts",
		"mus", "mws", "mxl", "mzp", "nap", "nki", "nra", "nrt", "nsa", "nsf", "nst", "ntn",
		"nvf",*/ "nwc", "odm", "oga", "ogg", /*"okt", "oma", "omf", "omg", "omx", "ots", "ove",
		"ovw", "pac", "pat", "pbf", "pca", "pcast", "pcg",*/ "pcm", /*"peak", "phy", "pk", "pla",
		"pls", "pna", "ppc", "ppcx", "prg", "prg", "psf", "psm", "ptf", "ptm", "pts", "pvc",
		"qcp", "r", "r1m", "ra", "ram", "raw", "rax", "rbs", "rcy", "rex", "rfl", "rmf", "rmi",
		"rmj", "rmm", "rmx", "rng", "rns", "rol", "rsn", "rso", "rti", "rtm", "rts", "rvx",
		"rx2", "s3i", "s3m", "s3z", "saf", "sam", "sb", "sbg", "sbi", "sbk", "sc2", "sd", "sd",
		"sd2", "sd2f", "sdat", "sdii", "sds", "sdt", "sdx", "seg", "seq", "ses", "sf", "sf2",
		"sfk", "sfl", "shn", "sib", "sid", "sid", "smf", "smp", "snd", "snd", "snd", "sng",
		"sng", "sou", "sppack", "sprg", "spx", "sseq", "sseq", "ssnd", "stm", "stx", "sty",
		"svx", "sw", "swa", "syh", "syw", "syx", "td0", "tfmx", "thx", "toc", "tsp", "txw",
		"u", "ub", "ulaw", "ult", "ulw", "uni", "usf", "usflib", "uw", "uwf", "vag", "val",
		"vc3", "vmd", "vmf", "vmf", "voc", "voi", "vox", "vpm", "vqf", "vrf", "vyf", "w01",
		"wav",*/ "wav", "wave", /*"wax", "wfb", "wfd", "wfp",*/ "wma", /*"wow", "wpk", "wproj", "wrk",
		"wus", "wut", "wv", "wvc", "wve", "wwu", "xa", "xa", "xfs", "xi", "xm", "xmf", "xmi",
		"xmz", "xp", "xrns", "xsb", "xspf", "xt", "xwb", "ym", "zvd", "zvr"*/ };
	
	private static final HashSet<String> mHashVideo;
	private static final HashSet<String> mHashAudio;
	
	private static final long KB = 1024;
	private static final long MB = 1024*1024;
	private static final long GB = 1024*1024*1024;
	
	static {
		mHashVideo = new HashSet<String>(Arrays.asList(VIDEO_EXTENSIONS));
		mHashAudio = new HashSet<String>(Arrays.asList(AUDIO_EXTENSIONS));
	}
	
	public static boolean isVideoOrAudio(File f) {
		final String ext = getFileExtension(f);
		return mHashAudio.contains(ext) || mHashVideo.contains(ext);
	}
	
	public static boolean isAudio(File f) {
	    final String ext = getFileExtension(f);
	    return mHashAudio.contains(ext);
	}
	
	public static boolean isVideo(File f) {
	    final String ext = getFileExtension(f);
        return mHashVideo.contains(ext);
	}
	
	public static boolean isVideoOrAudio(String url) {
        final String ext = getUrlExtension(url);
        return mHashAudio.contains(ext) || mHashVideo.contains(ext);
    }
    
    public static boolean isAudio(String url) {
        final String ext = getUrlExtension(url);
        return mHashAudio.contains(ext);
    }
    
    public static boolean isVideo(String url) {
        final String ext = getUrlExtension(url);
        return mHashVideo.contains(ext);
    }
	
	public static String getFileExtension(File f) {
		if (f != null) {
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1) {
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}
	
	public static String getUrlExtension(String url) {
	    if (!StringUtils.isEmpty(url)) {
	        int i = url.indexOf('.');
	        if (i > 0 && i < url.length() - 1) {
	            return url.substring(i+1).toLowerCase();
	        }
	    }
	    return "";
	}

	public static String getFileNameNoEx(String filename) {
	    if (filename != null && filename.length() > 0) {
	        int dot = filename.lastIndexOf('.');
	        if (dot > -1 && dot < filename.length()) {
	            return filename.substring(0, dot);
	        }
	    }
	    return filename;
	}
	
	/*
	 * show file size sting
	 */
	public static String showFileSize(long size) {
	    String fileSize="";
	    if (size < KB) {
	        fileSize = size + "B";
	    } else if (size < MB) {
	        fileSize = String.format("%.1f", (float)size / (float)KB) + "KB";
	    } else if (size < GB) {
	        fileSize = String.format("%.1f", (float)size / (float)MB) + "MB";
	    } else {
	        fileSize = String.format("%.1f", (float)size / (float)GB) + "GB";
	    }
	    
	    return fileSize;
	}
	
	/*
	 * show available size in sdcard
	 */
    public static String showSizeAvailable() {
	    String result = "";
	    if(Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
	        StatFs sf = new StatFs(Environment.getExternalStorageDirectory().getPath());
	        long blockSize = sf.getBlockSize();
	        long blockCount = sf.getBlockCount();
	        long availCount = sf.getAvailableBlocks();
	        return showFileSize(availCount*blockSize) + "/" + showFileSize(blockCount*blockSize);
	    }
	    return result;
	}
    
    /*
     * get thumbnail and video width/height
     */
    public static PFile getThumbnailAndWH(final Context ctx, File f) {
        PFile pf = new PFile();
        try {
            if (f.exists() && f.canRead()) {
                //get a thumbnail
                Log.i(TAG, "####### start createthumbnail" + f.getAbsolutePath());
                Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(ctx, f.getAbsolutePath(),
                        Video.Thumbnails.MINI_KIND);
                Log.i(TAG, "####### createthumbnail end");
                if (bitmap == null) {
                    bitmap = Bitmap.createBitmap(ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_WIDTH,
                            ThumbnailUtils.TARGET_SIZE_MINI_THUMBNAIL_HEIGHT, Bitmap.Config.RGB_565);
                    Log.e(TAG, "getThumbnailAndWH create thumbnail bitmap failed: " +
                            f.getAbsolutePath());
                }
                
                pf.width = bitmap.getWidth();
                pf.height = bitmap.getHeight();
                
                //thumbnail
                bitmap = ThumbnailUtils.extractThumbnail(bitmap,
                        ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_WIDTH,
                        ThumbnailUtils.TARGET_SIZE_MICRO_THUMBNAIL_HEIGHT,
                        ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
                
                if (bitmap != null) {
                    File thum = new File(f.getParent(), f.getName() + ".jpg");
                    pf.thumb = thum.getAbsolutePath();
                    FileOutputStream iStream = new FileOutputStream(thum);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 85, iStream);
                    iStream.close();
                    bitmap.recycle();
                }
                
            }
        } catch(Exception e) {
            Log.e(TAG, e.toString());
        }
        return pf;
    }
}
