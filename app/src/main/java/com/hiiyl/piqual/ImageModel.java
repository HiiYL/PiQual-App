package com.hiiyl.piqual;

import android.os.Parcel;
import android.os.Parcelable;

import com.orm.SugarRecord;

/**
 * Created by Suleiman19 on 10/22/15.
 */
public class ImageModel extends SugarRecord implements Parcelable {
    String name;
    String url;

    float rating;

    public ImageModel() {

    }

    protected ImageModel(Parcel in) {
        name = in.readString();
        url = in.readString();
        rating = in.readFloat();
        setId(in.readLong());
    }

    public ImageModel(String name, String url, String filePath) {
        this.name = name;
        this.url = url;
    }

    public static final Creator<ImageModel> CREATOR = new Creator<ImageModel>() {
        @Override
        public ImageModel createFromParcel(Parcel in) {
            return new ImageModel(in);
        }

        @Override
        public ImageModel[] newArray(int size) {
            return new ImageModel[size];
        }
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(url);
        dest.writeFloat(rating);
        dest.writeLong(getId());
    }

    public float getRating() {
        return (float)Math.round(rating * 10) / 10;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }
}
