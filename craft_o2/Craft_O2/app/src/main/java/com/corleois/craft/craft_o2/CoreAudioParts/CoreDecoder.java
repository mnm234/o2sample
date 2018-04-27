package com.corleois.craft.craft_o2.CoreAudioParts;

/**
 * Created by StarLink on 2017/06/17.
 */

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.support.annotation.IntegerRes;
import android.util.Log;

import com.corleois.craft.craft_o2.Activities.Interfaces.CoreDecorderNotyfication;
import com.corleois.craft.craft_o2.OriginalExceptions.InvalidFileTypeException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;

/**
 * デコーダーを担当するクラス。
 */
public class CoreDecoder {

    //初期設定等に使うデータ
    private String objname = "CoreDecoder";
    private MediaExtractor mediaUnBoxer;
    private HashMap<Integer,Boolean> validChkTable;
    private int minValidTrackNumber;
    private int maxValidTrackNumber;
    private int selectedTrackNumber;

    //デコードするときに使うデータ
    private MediaCodec codec;
    private ByteBuffer[] codecInputByteBuffers;
    private ByteBuffer[] codecOutputByteBuffers;
    private int sampleRate;
    private int channels;
    private long presentationTimeUs;
    private MediaFormat mediaFormat;
    private CoreDecorderNotyfication notyfication;  //デコーダーから通知をするときに使うクラス


    /**
     * コンストラクタ。デコードできないファイルだと判定されると例外を返します。
     * @param filePath  デコードしたいファイル
     * @throws Exception    様々な例外
     */
    public CoreDecoder(String filePath) throws IOException, InvalidFileTypeException {

        //コンテナからデータを引っ張ってくるクラスを作る
        mediaUnBoxer = new MediaExtractor();

        //展開してくれるオブジェクトに、何のデータを展開するかのデータを指定する
        mediaUnBoxer.setDataSource(filePath);
        //共通の初期化処理を呼び出す
        Init();
    }

    /**
     * コンストラクタ。デコードできないファイルだと判定されると例外を返します。
     * @param file  デコードしたいファイル
     * @throws Exception    様々な例外
     */
    public CoreDecoder(AssetFileDescriptor file) throws IOException, InvalidFileTypeException {

        //コンテナからデータを引っ張ってくるクラスを作る
        mediaUnBoxer = new MediaExtractor();

        //展開してくれるオブジェクトに、何のデータを展開するかのデータを指定する
        mediaUnBoxer.setDataSource(file.getFileDescriptor(),file.getStartOffset(),file.getLength());

        //共通の初期化処理を呼び出す
        Init();
    }

    /**
     * 共通の初期化処理
     * @throws Exception
     */
    private void Init() throws InvalidFileTypeException {
        //コーデックを明示的に初期化しておく
        codec = null;
        //サンプルレートも初期化して無効な値にしておく
        sampleRate = -1;
        //現在の再生時間(μ秒)も初期化して無効な値に
        presentationTimeUs = -1L;

        //チャンネル数も無効な値に
        channels = -1;

        //トラック数を獲得しておく
        int gotTrackCount = mediaUnBoxer.getTrackCount();
        Log.d(objname,"TrackCount:"+(gotTrackCount));

        //各トラックについて調べる。
        //映像のMIMEタイプなどを持っている場合も十分ありうるため、
        //各トラックが再生可能であるかどうかをチェックして記録する
        //（各トラック番号@int）,（[有効/無効]@bool）が構造
        validChkTable = new HashMap<>();

        //有効だったトラック番号の最大最小を格納する。
        //トラックの選択が行われずに再生を指示された場合に気を利かせるため
        minValidTrackNumber = -1;
        maxValidTrackNumber = -1;

        //コンテナ内に入っているファイルについて、それぞれデコードできるのかをチェックする。
        for(int i = 0; i < gotTrackCount; i++){
            MediaFormat format = mediaUnBoxer.getTrackFormat(i);
            String mimeType = format.getString(MediaFormat.KEY_MIME);
            Log.d(objname,"MIME type"+ (i) +" : " + (mimeType));

            //もし、mimeTypeがnullだったらどうしようもできないのでfalse
            if(mimeType == null){
                //ハッシュテーブルに有効かどうかを格納する。今回はnullだったので格納しない
                validChkTable.put(i, false);
            }else{
                //なにかMIMEタイプが読み取れたらこっち
                //MIMEタイプが音声ではない、映像の場合も考えられるため、
                // マーフィーの法則の従い、すべてがPCM形式にデコードできる前提で考えるとほぼ必ずクラッシュする。
                //もし、audio/～がmimeで読み取れたらTrue,そうでないときはfalseが自動的に入る
                validChkTable.put(i, mimeType.contains("audio/"));

                //有効なトラック番号が負数の場合、最初に見つけた再生できるトラックの発見
                //なので、その場合は値を更新する。
                minValidTrackNumber = (minValidTrackNumber < 0) ? i : minValidTrackNumber;

                //最大値も更新する
                maxValidTrackNumber = (maxValidTrackNumber < i) ? i : maxValidTrackNumber;
            }
        }

        //もし、有効なトラックが1つもなかったら、それ用に自作した例外を投げる
        if(minValidTrackNumber < 0){
            Log.e(objname,"デコードできるファイルではありませんでした");
            throw new InvalidFileTypeException();
        }

        //デフォルトは最初に発見されたトラック
        selectedTrackNumber = minValidTrackNumber;
        //トラックを選択しておく
        mediaUnBoxer.selectTrack(selectedTrackNumber);
    }

    /**
     * 検出された有効なトラック番号の最小値を返します。
     * 利用不可能な場合は-1が格納されています。
     * @return  トラック番号の最小値
     */
    public int getMinValidTrackNumber(){return minValidTrackNumber;}

    /**
     * 検出された有効なトラック番号の最大値を返します。
     * 利用不可能な場合は-1が格納されています。
     * @return トラック番号の最大値
     */
    public int getMaxValidTrackNumber(){return maxValidTrackNumber;}

    /**
     * オブジェクト生成時に検証されたトラック情報を返却します。
     * キー値は、Integer型でgetMinValidTrackNumber() ～ getMaxValidTrackNumber()の範囲を探索してください。
     * 値は再生可能かどうかのbooleanが獲得できます
     * @return  検証結果
     */
    public HashMap<Integer,Boolean> getValidChkTable(){return validChkTable;}

    /**
     * 利用するトラック番号を設定します。
     * 無効なトラック番号が設定された場合、設定されず、返り値はFalseです。
     * @param validIndex
     * @return 設定成功可否
     */
    public boolean selectTrackNumber(int validIndex){
        //もし、無効なキー情報が送られてきたら、Falseを返して何も変更しない
        if(!validChkTable.get(validIndex)){
            return false;
        }

        //現在の選択を解除する
        mediaUnBoxer.unselectTrack(selectedTrackNumber);
        //新しく選択を設定する
        mediaUnBoxer.selectTrack(validIndex);
        return true;
    }

    /**
     * このコード呼び出し以前に、同オブジェクトに設定された情報で、デコード処理を開始します。
     */
    public void startDecode() throws IOException {



        Log.d(objname,"デコード処理を開始します");
        //フォーマットからMIMEタイプを取得する
        mediaFormat = mediaUnBoxer.getTrackFormat(selectedTrackNumber);

        //お待ちかねのコーデック。
        //様々なフォーマットの音声データを、OSが処理できるPCMにデコードすることができる。
        // エンコード機能もついている、ありがたいローレベルAPI

        //作成したコーデックオブジェクトに、どう動作してもらうのかを設定する
        //まずは「取得したMIMEタイプ」に対応する「デコーダー」として作成する。
        codec = MediaCodec.createDecoderByType(mediaFormat.getString(MediaFormat.KEY_MIME));

        //より具体的に、どんなふうに処理するのかを設定する。
        codec.configure(
                mediaFormat,//コンテナに入っていた特定のトラックに関する情報を渡す
                null,       //動画を再生するときの映像の出力先を渡す引数。今回はもちろん使わない。
                null,       //著作権保護されたファイル関係。今回は一応対応しないが、今後機会があれば対応するかもしれない。
                0           //エンコーダーとして使用するかどうかのフラグを入れることができる。今回はデコーダーなので、どのフラグも立たない0
        );

        /**
         * After successfully configuring the component, call start.
         * Call start also if the codec is configured in asynchronous mode, and it has just been flushed, to resume requesting input buffers
         * 訳：startメソッドはコンポーネントの設定が成功した後に呼ばれるものである。
         * 　　また、コーデックが非同期モードで設定された後とか、フラッシュされた直後とか、入力バッファの復帰要求をしているときにも呼ばれる。
         */
        codec.start();

        //コーデックに通す入出力のバッファをそれぞれ用意する
        codecInputByteBuffers = codec.getInputBuffers();
        codecOutputByteBuffers = codec.getOutputBuffers();


        //サンプルレートを取得して設定しておく
        sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
        Log.d(objname,"SAMPLE_RATE:"+(Integer.toString(sampleRate)));

        //チャンネル数も取得して設定
        channels = mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT);
        Log.d(objname,"CHANNEL:"+Integer.toString(channels));

    }

    /**
     * デコードするファイルフォーマットのサンプルレートを取得します。
     * startDecode()メソッドを呼び出したあとにアクセスできるようになります。
     * それ以前に呼び出したり、処理に失敗した場合などは、-1が返されます。
     * @return  サンプルレート
     */
    public int getSampleRate(){ return sampleRate;}

    /**
     * デコードするファイルフォーマットのチャンネル数を取得します。
     * startDecode()メソッドを呼び出したあとにアクセスできるようになります。
     * それ以前に呼び出したり、処理に失敗した場合などは、-1が返されます。
     * @return
     */
    public int getChannels(){return channels;}


    /**
     * シークを行います。指定はマイクロ秒単位で行い、指定時間に最も近いキーフレームまで移動して再生します。
     * @param seekToUs  シーク先の再生時間（μ秒）。
     */
    public void seekTo(long seekToUs){mediaUnBoxer.seekTo(seekToUs,MediaExtractor.SEEK_TO_CLOSEST_SYNC);}


    /**
     * デコーダーに、デコード前のデータバッファを投げつけます。
     * 入力バッファが使えないタイミングに遭遇することがあるかもしれないので、
     * 引数にはそのようなときはどれだけの時間を待てるかを指定します。
     * 0は待ちません。正の数は指定された時間[マイクロ秒]だけ待ちます。
     * -1は、使えるようになるまでずーっと待ちます。
     *
     * このメソッドでコーデックにデータを投げ入れてデコード処理してもらいますので、
     * デコードされたデータを取ってくる前に、これを呼んでおく必要があります。
     *
     * 返り値は、この入力でEOFに到達したかを示します
     * @param canWaitTimeUs 待ち時間（気の長さ）
     * @return  ファイルの終端かどうか。Trueの場合終端
     */
    public boolean pushDataToDecoder(long canWaitTimeUs){

        //返り値になる、これでEOFかどうかを示す値。
        boolean endOfFile = false;
        /**
         * Returns the index of an input buffer to be filled with valid data or -1 if no such buffer is currently available.
         * 訳：有効なデータがみっちり書き込まれた入力バッファのインデックスか、
         *     あるいは呼び出し時点でそのようなバッファが使えない状態であるなら-1、
         *     その2つのうちどちらかが戻り値として返される。
         *
         * This method will return immediately if timeoutUs == 0, wait indefinitely for the availability of an input buffer if timeoutUs < 0 or wait up to "timeoutUs" microseconds if timeoutUs > 0.
         * 訳：このメソッドは引数のタイムアウト時間（マイクロ秒単位）が0に設定されていた場合、呼びされると即時に返り値が渡される。
         *     （入力バッファが利用可能でない場合）
         *     引数の時間が負数の場合、入力バッファ利用可能になるまでいつまでもずっと待機してから返す。
         *     引数の時間が正の数の場合、引数の値[マイクロ秒]だけ待機して、その時間を超過するまで待機してから返す。
         */
        int inputBufferIndex = codec.dequeueInputBuffer(canWaitTimeUs);

        if(inputBufferIndex >= 0){
            //有効なデータで満たされているはずの入力バッファのインデックスを指定してバッファを取ってくる
            ByteBuffer inBuffer = codecInputByteBuffers[inputBufferIndex];

            /**
             * Retrieve the current encoded sample and store it in the bytebuffer starting at the given offset.
             * 訳：現在の符号化されたサンプルを取得し、引数のオフセットから始まるバイトバッファに格納する。
             *
             * 返り値はこれ以上有効なサンプルがないときは-1を返す。
             *
             * つまり圧縮されたままのデータのコードをファイルから引っ張ってきて、引数のバッファに入れてくれる
             * 返り値はそのバッファのサイズ。
             */
            int buffersize = mediaUnBoxer.readSampleData(inBuffer,0);

            //もし、有効なサンプルデータがなかった場合は、-1が返ってくるのでT。
            //それはストリームの終端ということ。
            if(buffersize < 0){
                endOfFile = true;
                buffersize = 0;
            }else {
                //有効なデータのときはこっち
                //現在のサンプルが示す再生時間を取得して格納する
                presentationTimeUs = mediaUnBoxer.getSampleTime();
            }
            //入力バッファをデコーダーの入り口に流し込む
            codec.queueInputBuffer(
                    inputBufferIndex,   //流し込むバッファのインデックス
                    0,                  //バッファの開始オフセット。
                    buffersize,         //流し込むバッファのサイズ
                    presentationTimeUs, //サンプルが示す再生時間
                    //バッファのフラグ指定。最後なら最後のフラグを、そうでないなら特に指定なし。必要に応じてドキュメントを読むこと。
                    (endOfFile ? MediaCodec.BUFFER_FLAG_END_OF_STREAM : 0)
            );

            //ファイル終端でないなら、続ける処理の準備をする
            if(!endOfFile){
                /* メモ：テスト時のシーク成功箇所はここ　*/

                //コンテナのトラックから次のバッファデータを準備するよう送信。
                mediaUnBoxer.advance();
            }
        }
        return endOfFile;
    }

    /**
     * デコーダーからにゅるんと出てきたデコード済みのデータをbyte[]形式で取得します。
     *
     * デコードが追いついてないみたいな、バッファが使えないタイミングに遭遇することがあるかもしれないので、
     * 引数にはそのようなときはどれだけの時間を待てるかを指定します。
     * 0は待ちません。正の数は指定された時間[マイクロ秒]だけ待ちます。
     * -1は、使えるようになるまでずーっと待ちます。
     *
     * デコードされたデータのフォーマットが変更されることがあるかもしれないので、
     * 呼び出し後、getSampleRate()でサンプルレートをチェックしてください。
     *
     * 当然、このメソッドでデータを取ってくる前に、デコーダーに元データを押し込んでおく必要があります。
     *
     * @param canWaitTimeUs 待ち時間
     * @return  デコードされたデータ
     */
    public byte[] pullDataFromDecoder(long canWaitTimeUs){
        //メディアコーデックのバッファ情報を取得するためのオブジェクトを作成する
        MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
        //帰り値のデータを保持する
        byte[] chunk = null;

        //デコーダーからのデコード済みの出力バッファのインデックスを獲得する
        //引数は、獲得するバッファのメタデータを入れる空箱と、データが取り出せないときに待つ上限時間設定(マイクロ秒)
        int outputBufIndex = codec.dequeueOutputBuffer(info,canWaitTimeUs);
//        Log.d(objname,"outputBufIndex = " +(outputBufIndex));

        //バッファが有効な時の場合はTになるはず
        if(outputBufIndex >= 0){
            //コーデックが持ってるバッファの中から、用意されたバッファのインデックスを指定して取得する
            ByteBuffer buffer = codecOutputByteBuffers[outputBufIndex];

            //バッファを、獲得できそうなバッファのサイズだけ作成
            chunk = new byte[info.size];

            //取得したバッファデータを、引数のbyte配列にコピーする
            buffer.get(chunk);
            //受け皿として使ったバッファをクリアする
            buffer.clear();

            //返り値に押し込んだので、デコードされたデータを解放する
            //引数は解放するバッファのインデックスと、動画用の何かしらのフラグ。後者はFで
            codec.releaseOutputBuffer(outputBufIndex,false);

        }else if(outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED){
            //入力バッファが変わったときは、もう一度呼んでおく
            codecOutputByteBuffers = codec.getOutputBuffers();
        }else if(outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED){
            //出力フォーマットが変わったときは、出力フォーマットを再設定する
            mediaFormat = codec.getOutputFormat();
            //サンプルレートも再設定
            sampleRate = mediaFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE);
            Log.d(objname,"コーデック出力フォーマット変更検知 SampleRate："+(sampleRate));
            if(notyfication != null){
                notyfication.onOutPutFormatChanged(mediaFormat);
            }
        }
        return chunk;
    }

    public long getSampleTime(){return mediaUnBoxer.getSampleTime();}

    /**
     * 用済みのデコーダーオブジェクトを解放します。
     * メモリリークや不具合の原因になりうるので、別のデコーダーを新しく宣言する前に、必ずこのメソッドを呼び出してください。
     */
    public void dispose(){
        //コンテナをから操作してくれたオブジェクトも解放する
        mediaUnBoxer.release();
        mediaUnBoxer = null;
        //コーデックも使い終わったので終了して解放する
        codec.stop();
        codec.reset();
        codec.release();
        codec = null;
    }

    public void setCoreDecorderNotificationListener(CoreDecorderNotyfication listener){
        this.notyfication = listener;
    }
}

