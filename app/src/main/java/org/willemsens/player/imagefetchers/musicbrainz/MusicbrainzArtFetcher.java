package org.willemsens.player.imagefetchers.musicbrainz;

import okhttp3.HttpUrl;
import org.willemsens.player.exceptions.PlayerException;
import org.willemsens.player.imagefetchers.AlbumInfo;
import org.willemsens.player.imagefetchers.ArtFetcher;
import org.willemsens.player.imagefetchers.ArtistInfo;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ArtistsResponse;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ImagesReponse;
import org.willemsens.player.imagefetchers.musicbrainz.dto.Release;
import org.willemsens.player.imagefetchers.musicbrainz.dto.ReleasesResponse;
import org.willemsens.player.model.ImageSource;

public class MusicbrainzArtFetcher extends ArtFetcher {
    @Override
    public String fetchArtistId(String artistName) {
        final HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("musicbrainz.org")
                .addPathSegment("ws")
                .addPathSegment("2")
                .addPathSegment("artist")
                .addQueryParameter("query", "artist:" + artistName)
                .build();
        final String json = fetch(url);
        String artistId = null;
        if (json != null) {
            ArtistsResponse artistsResponse = getGson().fromJson(
                    json,
                    ArtistsResponse.class);
            artistId = artistsResponse.getFirstArtistID();
        }

        return artistId;
    }

    @Override
    public AlbumInfo fetchAlbumInfo(String artistName, String albumName) {
        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("musicbrainz.org")
                .addPathSegment("ws")
                .addPathSegment("2")
                .addPathSegment("release")
                //.addQueryParameter("query", "release:" + albumName + " AND arid:" + artistId)
                .addQueryParameter("query", "release:" + albumName + " AND artist:" + artistName)
                .build();

        String json = fetch(url);
        if (json != null) {
            ReleasesResponse releasesResponse = getGson().fromJson(
                    json,
                    ReleasesResponse.class);
            Release[] releases = releasesResponse.getReleases();

            if (releases != null) {
                for (Release release : releases) {
                    url = new HttpUrl.Builder()
                            .scheme("http")
                            .host("coverartarchive.org")
                            .addPathSegment("release")
                            .addPathSegment(release.getId())
                            .build();

                    json = fetch(url);
                    if (json != null) {
                        ImagesReponse imagesReponse = getGson().fromJson(json, ImagesReponse.class);

                        return new AlbumInfo(imagesReponse.getFirstLargeThumbnail(), releasesResponse.getOldestReleaseYear());
                    }
                }
            }
        }

        return null;
    }

    @Override
    public ArtistInfo fetchArtistInfo(String artistId) {
        throw new PlayerException("Fetching artist images is not supported by Musicbrainz.");
    }

    @Override
    public ImageSource getImageSource() {
        return ImageSource.MUSICBRAINZ;
    }
}
