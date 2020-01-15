# gitlab-issues-paster

An application that outputs a list of GitLab issues to Google Spread Sheet to improve browsability and searchability.

## Usage

1. 以下のページを参考に Google Sheets API の有効化と credentials.json のダウンロードをしてください。
    * https://developers.google.com/sheets/api/quickstart/java#step_1_turn_on_the
1. 取得した credential.json は src/main/resources 配下にコピーしてください。
1. 以下 4 つの環境変数を設定してください。
    * `GITLAB_PRIVATE_TOKEN` (ref: https://docs.gitlab.com/ee/user/profile/personal_access_tokens.html)
    * `GITLAB_DOMAIN_NAME` (ex: gitlab.xxx.com)
    * `GITLAB_PROJECT_ID`
    * `GOOGLE_SPREAD_SHEET_ID`
1. `sbt run` でアプリケーションを起動し、しばらくすると OAuth 同意画面がブラウザに表示されるので認可してください。

## Notes

認可後のアクセストークンは /tmp/.google_credentials 配下に置かれるようになっています。
消しても再認可すればいいので特に問題はありません。