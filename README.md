# Manage_bot

이 봇은 그룹방을 관리해 줍니다.

웹훅을 등록해야 합니다.<br>
텔레그램 api 페이지를 참고하여 <br>https://&lt;hostname&gt;/Telegram<br>
으로 등록 해 주세요<br>

https를 위한 인증서가 필요하며 .jks로 되어있어야 합니다.

config.txt의 hostname, key_path, key_password, Telegram_token은 필수 항목입니다.

key_path에는 인증서의 위치, key_password에는 인증서의 비밀번호가 들어갑니다.

/hitomi - 품번을 url로 전환하고 첫 번째 페이지의 이미지를 전송합니다.<br>
/gethitomi - 품번을 사용하여 작품을 다운로드 합니다.<br>
/mute - 멘션한 사용자의 메시지 전송권한을 해지합니다. 봇에 관리자 권한이 필요합니다.<br>
/unmute - 뮤트를 해제합니다. 봇에 관리자 권한이 필요합니다.<br>
/banchat - 특정 텍스트를 밴합니다. 봇에 관리자 권한이 필요합니다.<br>
/unbanchat - 벤한 텍스트를 해제합니다.<br>
/getbanchat - 벤한 텍스트를 확인합니다.<br>
/bansticker - 개별 스티커를 밴합니다. 봇에 관리자 권한이 필요합니다.<br>
/banset - 스티커 세트를 밴합니다. 봇에 관리자 권한이 필요합니다.<br>
/unbansticker - 개별 스티커 밴을 해제합니다.<br>
/unbanset - 스티커 세트 밴을 해제합니다.<br>
/dccon <디시콘 번호> - 해당 번호에 해당하는 디시콘을 스티커로 변환한다.

