VIDEO_TEMPLATE="%(title)s.%(ext)s"
TEMPLATE="$VIDEO_TEMPLATE"
POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    -P|--path)
      LOCATION="$2"
      shift # past argument
      shift # past value
      ;;
    -v|--video)
      VIDEO="$2"
      shift # past argument
      shift # past value
      ;;
    -s|--season)
      PLAYLIST_SEASON="$2"
      TEMPLATE="%(playlist)s/S${PLAYLIST_SEASON}E%(playlist_index)s - %(title)s.%(ext)s"
      shift # past argument
      shift # past value
      ;;
    -*|--*)
      echo "Unknown option $1"
      exit 1
      ;;
    *)
      POSITIONAL_ARGS+=("$1") # save positional arg
      shift # past argument
      ;;
  esac
done

if [ -z "$VIDEO" ] || [ -z "$LOCATION" ]; then
    echo "USAGE: -P {LOCATION} -v {VIDEO} -s {SEASON (ex. '02')}"
    exit 0
fi


yt-dlp -S res,ext:mp4:m4a \
 --recode mp4 \
 --audio-quality 10 -N 32 \
 -P "$LOCATION" \
 -o "$TEMPLATE" \
 "$VIDEO"